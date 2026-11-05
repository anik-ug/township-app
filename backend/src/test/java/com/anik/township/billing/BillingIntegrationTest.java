package com.anik.township.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
class BillingIntegrationTest {

    @Autowired
    BillingService billingService;

    @Test
    void seededResidentsHaveExpectedAsOfBalances() {
        BillingService.BillView t12 = billingService.bills(2L).stream()
                .filter(bill -> bill.billMonth().equals(LocalDate.of(2026, 9, 1)))
                .findFirst()
                .orElseThrow();
        BillingService.BillView t21 = billingService.bills(3L).stream()
                .filter(bill -> bill.billMonth().equals(LocalDate.of(2026, 10, 1)))
                .findFirst()
                .orElseThrow();

        assertEquals(637000 + 7749, t12.totalDuePaise());
        assertEquals(566000 + 1489, t21.totalDuePaise());
    }

    @Test
    @Transactional
    void t21LedgerAddsLateFeeAfterPaymentsAndEndsAtHomeBalance() {
        billingService.pay(3L, 9L, 100000);

        var ledger = billingService.ledger(3L);
        var entries = ledger.subList(ledger.size() - 4, ledger.size());

        assertEquals("BILL", entries.get(0).type());
        assertEquals(1066000, entries.get(0).debitPaise());
        assertEquals("PAYMENT", entries.get(1).type());
        assertEquals(500000, entries.get(1).creditPaise());
        assertEquals("LATE_FEE", entries.get(2).type());
        assertEquals(1489, entries.get(2).debitPaise());
        assertEquals("PAYMENT", entries.get(3).type());
        assertEquals(100000, entries.get(3).creditPaise());
        assertEquals(467489, entries.get(3).runningBalance());

        assertEquals(467489, billingService.home(3L).totalDuePaise());
        assertEquals(467489, ledger.get(ledger.size() - 1).runningBalance());
    }
}
