package com.anik.township.committee;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anik.township.domain.ComplaintStatus;
import com.anik.township.domain.BankRowStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardCalculatorTest {
    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 5);
    private static final OffsetDateTime NOW = AS_OF.atStartOfDay().atOffset(ZoneOffset.UTC);
    private final DashboardCalculator calculator = new DashboardCalculator();

    @Test
    void ageingUsesInclusiveBucketEdgesAndComputedLateFee() {
        List<DashboardCalculator.BillData> bills = List.of(
                bill(1, "A", AS_OF.minusDays(30)), bill(2, "B", AS_OF.minusDays(31)),
                bill(3, "C", AS_OF.minusDays(60)), bill(4, "D", AS_OF.minusDays(61)),
                bill(5, "E", AS_OF.minusDays(90)), bill(6, "F", AS_OF.minusDays(91)));
        var result = calculate(bills, List.of());
        assertEquals(1, result.duesAgeing().buckets().get("0-30").billCount());
        assertEquals(2, result.duesAgeing().buckets().get("31-60").billCount());
        assertEquals(2, result.duesAgeing().buckets().get("61-90").billCount());
        assertEquals(1, result.duesAgeing().buckets().get("90+").billCount());

        var feeResult = calculate(List.of(new DashboardCalculator.BillData(
                7, "fee", LocalDate.of(2026, 9, 1), 100_000, AS_OF.minusDays(31))), List.of());
        assertEquals(101_529,
                feeResult.duesAgeing().totalOutstandingPaise());
    }

    @Test
    void complaintTimesUseStatusEventsAndEmptyComplaintsAreSafe() {
        var created = AS_OF.atStartOfDay().minusDays(1).atOffset(ZoneOffset.UTC);
        var complaints = List.of(new DashboardCalculator.ComplaintData(
                1, "LIFT", ComplaintStatus.RESOLVED, true, created, 1));
        var events = List.of(
                new DashboardCalculator.ComplaintEventData(1, ComplaintStatus.ASSIGNED, created.plusMinutes(5)),
                new DashboardCalculator.ComplaintEventData(1, ComplaintStatus.IN_PROGRESS, created.plusMinutes(65)),
                new DashboardCalculator.ComplaintEventData(1, ComplaintStatus.RESOLVED, created.plusMinutes(120)));
        var result = calculator.calculate(List.of(), List.of(), complaints, events, List.of(),
                AS_OF, NOW, 0, 1800, null, 240);
        assertEquals("65.00", result.complaints().avgFirstResponseMinutes().toPlainString());
        assertEquals("2.00", result.complaints().avgResolutionHours().toPlainString());
        assertEquals("100.00", result.complaints().reopenedPct().toPlainString());
        assertEquals(0, result.complaints().responseExcludedCount());
        assertEquals(0, result.complaints().resolutionExcludedCount());

        var empty = calculate(List.of(), List.of());
        assertEquals("0.00", empty.complaints().reopenedPct().toPlainString());
        assertEquals("0.00", empty.complaints().avgResolutionHours().toPlainString());
    }

    private DashboardCalculator.Dashboard calculate(
            List<DashboardCalculator.BillData> bills, List<DashboardCalculator.PaymentData> payments) {
        return calculator.calculate(bills, payments, List.of(), List.of(),
                List.of(new DashboardCalculator.BankRowData(BankRowStatus.QUEUED, 1)),
                AS_OF, NOW, 0, 1800, null, 240);
    }

    private static DashboardCalculator.BillData bill(long id, String flat, LocalDate dueDate) {
        return new DashboardCalculator.BillData(id, flat, dueDate.withDayOfMonth(1), 100_000, dueDate);
    }
}
