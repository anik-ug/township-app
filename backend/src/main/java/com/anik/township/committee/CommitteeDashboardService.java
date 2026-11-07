package com.anik.township.committee;

import com.anik.township.config.DateProvider;
import com.anik.township.domain.BankRowStatus;
import com.anik.township.repository.BankRowRepository;
import com.anik.township.repository.BillRepository;
import com.anik.township.repository.ComplaintEventRepository;
import com.anik.township.repository.ComplaintRepository;
import com.anik.township.repository.LateFeeRuleRepository;
import com.anik.township.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommitteeDashboardService {
    private final BillRepository bills;
    private final PaymentRepository payments;
    private final ComplaintRepository complaints;
    private final ComplaintEventRepository events;
    private final BankRowRepository bankRows;
    private final LateFeeRuleRepository lateFeeRules;
    private final DateProvider dateProvider;
    private final int slaResponseMinutes;
    private final DashboardCalculator calculator = new DashboardCalculator();

    public CommitteeDashboardService(BillRepository bills, PaymentRepository payments,
            ComplaintRepository complaints, ComplaintEventRepository events, BankRowRepository bankRows,
            LateFeeRuleRepository lateFeeRules, DateProvider dateProvider,
            @Value("${app.sla-response-minutes:240}") int slaResponseMinutes) {
        this.bills = bills;
        this.payments = payments;
        this.complaints = complaints;
        this.events = events;
        this.bankRows = bankRows;
        this.lateFeeRules = lateFeeRules;
        this.dateProvider = dateProvider;
        this.slaResponseMinutes = slaResponseMinutes;
    }

    @Transactional(readOnly = true)
    public DashboardCalculator.Dashboard dashboard() {
        var asOf = dateProvider.asOfDate();
        var now = dateProvider.now();
        var rule = lateFeeRules.findFirstByActiveTrueOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("No active late fee rule configured"));
        return calculator.calculate(
                bills.findAllByBillMonthLessThanEqual(asOf).stream()
                        .map(b -> new DashboardCalculator.BillData(b.getId(), b.getFlat().getFlatNo(),
                                b.getBillMonth(), b.getAmountPaise(), b.getDueDate())).toList(),
                payments.findAllByPaidAtLessThanEqual(now).stream()
                        .map(p -> new DashboardCalculator.PaymentData(p.getBill().getId(), p.getAmountPaise(),
                                p.getLateFeePaise(), p.getPaidAt())).toList(),
                complaints.findAllWithFlat().stream()
                        .map(c -> new DashboardCalculator.ComplaintData(c.getId(), c.getCategory(), c.getStatus(),
                                c.isEscalated(), c.getCreatedAt(), c.getReopenCount())).toList(),
                events.findAllWithComplaint().stream()
                        .map(e -> new DashboardCalculator.ComplaintEventData(e.getComplaint().getId(),
                                e.getToStatus(), e.getEventAt())).toList(),
                bankRows.findByStatusOrderByTxnDateAscIdAsc(BankRowStatus.QUEUED).stream()
                        .map(r -> new DashboardCalculator.BankRowData(r.getStatus(), r.getAmountPaise())).toList(),
                asOf, now, rule.getGraceDays(), rule.getAnnualRateBps(), rule.getCapPaise(),
                slaResponseMinutes);
    }
}
