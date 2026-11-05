package com.anik.township.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.anik.township.config.DateProvider;
import com.anik.township.domain.Bill;
import com.anik.township.domain.LateFeeRule;
import com.anik.township.domain.Payment;
import com.anik.township.repository.BillRepository;
import com.anik.township.repository.ComplaintRepository;
import com.anik.township.repository.LateFeeRuleRepository;
import com.anik.township.repository.PaymentRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BillingServiceTest {

    @Mock BillRepository billRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock LateFeeRuleRepository lateFeeRuleRepository;
    @Mock ComplaintRepository complaintRepository;
    @Mock DateProvider dateProvider;

    private Bill bill;
    private BillingService service;

    @BeforeEach
    void setUp() {
        service = new BillingService(billRepository, paymentRepository, lateFeeRuleRepository,
                complaintRepository, dateProvider);
        bill = new Bill();
        bill.setId(1L);
        bill.setBillMonth(LocalDate.of(2026, 9, 1));
        bill.setDueDate(LocalDate.of(2026, 9, 19));
        bill.setAmountPaise(637000);
        com.anik.township.domain.Flat flat = new com.anik.township.domain.Flat();
        flat.setId(2L);
        flat.setFlatNo("T12-0408");
        bill.setFlat(flat);
        LateFeeRule rule = new LateFeeRule();
        rule.setGraceDays(10);
        rule.setAnnualRateBps(1200);
        when(lateFeeRuleRepository.findFirstByActiveTrueOrderByIdAsc()).thenReturn(Optional.of(rule));
        when(dateProvider.asOfDate()).thenReturn(LocalDate.of(2026, 11, 5));
        when(dateProvider.now()).thenReturn(OffsetDateTime.parse("2026-11-05T10:00:00+05:30"));
        when(paymentRepository.findByBillIdOrderByPaidAtAscIdAsc(1L)).thenReturn(List.of());
        when(billRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(55L);
            return payment;
        });
    }

    @Test
    void allocatesPaymentToLateFeeFirst() {
        BillingService.Receipt receipt = service.pay(2L, 1L, 8000);
        assertEquals(7749, receipt.lateFeePaise());
        assertEquals(251, receipt.principalPaise());
    }

    @Test
    void payingTotalDueLeavesNoBalance() {
        BillingService.Receipt receipt = service.pay(2L, 1L, 644749);
        assertEquals(0, receipt.remainingDuePaise());
    }

    @Test
    void rejectsOverpaymentAndNonPositiveAmounts() {
        assertThrows(ResponseStatusException.class, () -> service.pay(2L, 1L, 645000));
        assertThrows(ResponseStatusException.class, () -> service.pay(2L, 1L, 0));
        assertThrows(ResponseStatusException.class, () -> service.pay(2L, 1L, -1));
    }

    @Test
    void returnsNotFoundForAnotherFlat() {
        assertEquals(404, assertThrows(ResponseStatusException.class,
                () -> service.pay(99L, 1L, 100)).getStatusCode().value());
    }

    @Test
    void paidPastDueBillIsNotOverdueAndDoesNotAffectDashboardOldestOverdueDays() {
        Payment payment = new Payment();
        payment.setAmountPaise(637000);
        payment.setLateFeePaise(0);
        when(paymentRepository.findByBillIdOrderByPaidAtAscIdAsc(1L)).thenReturn(List.of(payment));
        when(billRepository.findByFlatIdOrderByBillMonthDesc(2L)).thenReturn(List.of(bill));
        when(complaintRepository.countByFlatIdAndStatusIn(any(), any())).thenReturn(0L);

        BillingService.BillView billView = service.bills(2L).get(0);
        BillingService.HomeResponse home = service.home(2L);

        assertEquals(0, billView.totalDuePaise());
        assertEquals(0, billView.daysOverdue());
        assertEquals(0, home.oldestOverdueDays());
    }
}
