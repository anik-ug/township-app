package com.anik.township.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class LateFeeCalculatorTest {

    private static final LocalDate AS_OF = LocalDate.of(2026, 11, 5);

    @Test
    void calculatesSeedLateFees() {
        assertEquals(7749, fee(637000, LocalDate.of(2026, 9, 19)));
        assertEquals(1489, fee(566000, LocalDate.of(2026, 10, 18)));
    }

    @Test
    void appliesGracePeriod() {
        assertEquals(0, fee(100000, AS_OF.minusDays(10)));
        assertEquals(1, LateFeeCalculator.calculate(365, AS_OF.minusDays(11), AS_OF, 10, 5000, null));
    }

    @Test
    void notYetDueHasNoFee() {
        assertEquals(0, fee(100000, AS_OF.plusDays(1)));
    }

    @Test
    void appliesCap() {
        assertEquals(100, LateFeeCalculator.calculate(
                637000, LocalDate.of(2026, 9, 19), AS_OF, 10, 1200, 100L));
    }

    @Test
    void halfRoundsUp() {
        assertEquals(1, LateFeeCalculator.calculate(
                365, AS_OF.minusDays(11), AS_OF, 10, 5000, null));
    }

    private long fee(long principal, LocalDate dueDate) {
        return LateFeeCalculator.calculate(principal, dueDate, AS_OF, 10, 1200, null);
    }
}
