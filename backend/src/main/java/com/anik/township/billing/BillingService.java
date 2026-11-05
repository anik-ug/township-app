package com.anik.township.billing;

import com.anik.township.config.DateProvider;
import com.anik.township.domain.Bill;
import com.anik.township.domain.BillStatus;
import com.anik.township.domain.ComplaintStatus;
import com.anik.township.domain.LateFeeRule;
import com.anik.township.domain.Payment;
import com.anik.township.domain.PaymentMethod;
import com.anik.township.repository.BillRepository;
import com.anik.township.repository.ComplaintRepository;
import com.anik.township.repository.LateFeeRuleRepository;
import com.anik.township.repository.PaymentRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final LateFeeRuleRepository lateFeeRuleRepository;
    private final ComplaintRepository complaintRepository;
    private final DateProvider dateProvider;

    public BillingService(
            BillRepository billRepository,
            PaymentRepository paymentRepository,
            LateFeeRuleRepository lateFeeRuleRepository,
            ComplaintRepository complaintRepository,
            DateProvider dateProvider) {
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.lateFeeRuleRepository = lateFeeRuleRepository;
        this.complaintRepository = complaintRepository;
        this.dateProvider = dateProvider;
    }

    @Transactional(readOnly = true)
    public HomeResponse home(long flatId) {
        List<BillView> bills = billViews(flatId);
        BillView next = bills.stream()
                .filter(bill -> bill.totalDuePaise() > 0)
                .min(Comparator.comparing(BillView::dueDate))
                .orElse(null);
        int oldestOverdueDays = bills.stream()
                .filter(bill -> bill.totalDuePaise() > 0)
                .mapToInt(BillView::daysOverdue)
                .max()
                .orElse(0);
        long openComplaints = complaintRepository.countByFlatIdAndStatusIn(
                flatId, List.of(ComplaintStatus.OPEN, ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS));
        return new HomeResponse(
                bills.isEmpty() ? null : bills.get(0).flatNo(),
                bills.stream().mapToLong(BillView::totalDuePaise).sum(),
                next == null ? null : next.dueDate(),
                oldestOverdueDays,
                openComplaints);
    }

    @Transactional(readOnly = true)
    public List<BillView> bills(long flatId) {
        return billViews(flatId);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> ledger(long flatId) {
        List<LedgerEntry> entries = new ArrayList<>();
        for (Bill bill : billRepository.findByFlatIdOrderByBillMonthDesc(flatId)) {
            entries.add(new LedgerEntry(bill.getBillMonth(), "BILL", bill.getAmountPaise(), 0));
            List<Payment> payments = paymentRepository.findByBillIdOrderByPaidAtAscIdAsc(bill.getId());
            for (Payment payment : payments) {
                entries.add(new LedgerEntry(payment.getPaidAt().toLocalDate(), "PAYMENT", 0, payment.getAmountPaise()));
            }
            BillAmounts amounts = amounts(bill);
            long lateFeeChargedPaise = payments.stream().mapToLong(Payment::getLateFeePaise).sum()
                    + amounts.lateFeeDuePaise();
            if (lateFeeChargedPaise > 0) {
                entries.add(new LedgerEntry(
                        dateProvider.asOfDate(), "LATE_FEE", lateFeeChargedPaise, 0));
            }
        }
        entries.sort(Comparator.comparing(LedgerEntry::entryDate).thenComparing(LedgerEntry::type));
        long runningBalance = 0;
        List<LedgerEntry> result = new ArrayList<>();
        for (LedgerEntry entry : entries) {
            runningBalance += entry.debitPaise() - entry.creditPaise();
            result.add(new LedgerEntry(entry.entryDate(), entry.type(), entry.debitPaise(),
                    entry.creditPaise(), runningBalance));
        }
        return result;
    }

    @Transactional
    public Receipt pay(long flatId, long billId, long amountPaise) {
        if (amountPaise <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amountPaise must be greater than zero");
        }
        Bill bill = billRepository.findByIdForUpdate(billId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found"));
        if (!bill.getFlat().getId().equals(flatId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bill not found");
        }
        BillAmounts amounts = amounts(bill);
        if (amountPaise > amounts.totalDuePaise()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment exceeds total due");
        }
        // Allocate every payment to the computed late fee first, then to principal.
        long lateFeePaise = Math.min(amountPaise, amounts.lateFeeDuePaise());
        long principalPaise = amountPaise - lateFeePaise;
        Payment payment = new Payment();
        payment.setBill(bill);
        payment.setAmountPaise(amountPaise);
        payment.setLateFeePaise(lateFeePaise);
        payment.setMethod(PaymentMethod.TEST_ONLINE);
        payment.setReference("TEST-" + UUID.randomUUID());
        payment.setPaidAt(dateProvider.now());
        Payment saved = paymentRepository.save(payment);
        return receipt(saved, amounts.totalDuePaise() - amountPaise);
    }

    @Transactional(readOnly = true)
    public Receipt receipt(long flatId, long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found"));
        if (!payment.getBill().getFlat().getId().equals(flatId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        return receipt(payment, amounts(payment.getBill()).totalDuePaise());
    }

    private List<BillView> billViews(long flatId) {
        List<Bill> bills = billRepository.findByFlatIdOrderByBillMonthDesc(flatId);
        return bills.stream().map(this::billView).toList();
    }

    private BillView billView(Bill bill) {
        BillAmounts amounts = amounts(bill);
        LocalDate asOf = dateProvider.asOfDate();
        int daysOverdue = amounts.totalDuePaise() > 0 && asOf.isAfter(bill.getDueDate())
                ? (int) (asOf.toEpochDay() - bill.getDueDate().toEpochDay())
                : 0;
        return new BillView(bill.getId(), bill.getFlat().getFlatNo(), bill.getBillMonth(),
                bill.getAmountPaise(), amounts.paidPaise(), amounts.lateFeeDuePaise(),
                amounts.totalDuePaise(), status(bill, amounts), daysOverdue, bill.getDueDate());
    }

    private BillAmounts amounts(Bill bill) {
        LateFeeRule rule = lateFeeRuleRepository.findFirstByActiveTrueOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("No active late fee rule configured"));
        List<Payment> payments = paymentRepository.findByBillIdOrderByPaidAtAscIdAsc(bill.getId());
        long paidPaise = payments.stream().mapToLong(Payment::getAmountPaise).sum();
        long lateFeePaidPaise = payments.stream().mapToLong(Payment::getLateFeePaise).sum();
        long principalPaidPaise = payments.stream().mapToLong(p -> p.getAmountPaise() - p.getLateFeePaise()).sum();
        long principalBalance = Math.max(0, bill.getAmountPaise() - principalPaidPaise);
        long computedFee = LateFeeCalculator.calculate(principalBalance, bill.getDueDate(),
                dateProvider.asOfDate(), rule.getGraceDays(), rule.getAnnualRateBps(), rule.getCapPaise());
        long lateFeeDue = Math.max(0, computedFee - lateFeePaidPaise);
        return new BillAmounts(paidPaise, principalBalance, lateFeeDue, principalBalance + lateFeeDue);
    }

    private BillStatus status(Bill bill, BillAmounts amounts) {
        if (amounts.totalDuePaise() == 0) {
            return BillStatus.PAID;
        }
        if (amounts.paidPaise() > 0 && !dateProvider.asOfDate().isAfter(bill.getDueDate())) {
            return BillStatus.PARTIAL;
        }
        if (dateProvider.asOfDate().isAfter(bill.getDueDate())) {
            return BillStatus.OVERDUE;
        }
        return BillStatus.UNPAID;
    }

    private Receipt receipt(Payment payment, long remainingDuePaise) {
        Bill bill = payment.getBill();
        return new Receipt(payment.getId(), "RCP-" + payment.getId(), bill.getBillMonth(),
                bill.getFlat().getFlatNo(), payment.getAmountPaise(), payment.getLateFeePaise(),
                payment.getAmountPaise() - payment.getLateFeePaise(), payment.getPaidAt(), remainingDuePaise);
    }

    private record BillAmounts(long paidPaise, long principalBalance, long lateFeeDuePaise, long totalDuePaise) {
    }

    public record HomeResponse(
            String flatNo, long totalDuePaise, LocalDate nextDueDate, int oldestOverdueDays, long openComplaintCount) {
    }

    public record BillView(
            Long id, String flatNo, LocalDate billMonth, long amountPaise, long paidPaise, long lateFeePaise,
            long totalDuePaise, BillStatus status, int daysOverdue, LocalDate dueDate) {
    }

    public record LedgerEntry(
            LocalDate entryDate, String type, long debitPaise, long creditPaise, long runningBalance) {
        public LedgerEntry(LocalDate entryDate, String type, long debitPaise, long creditPaise) {
            this(entryDate, type, debitPaise, creditPaise, 0);
        }
    }

    public record Receipt(
            Long paymentId, String receiptNo, LocalDate billMonth, String flatNo, long amountPaise,
            long lateFeePaise, long principalPaise, OffsetDateTime paidAt, long remainingDuePaise) {
    }
}
