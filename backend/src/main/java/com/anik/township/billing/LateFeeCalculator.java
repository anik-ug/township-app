package com.anik.township.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public final class LateFeeCalculator {

    private static final BigDecimal TEN_THOUSAND = BigDecimal.valueOf(10_000);
    private static final BigDecimal DAYS_PER_YEAR = BigDecimal.valueOf(365);

    private LateFeeCalculator() {
    }

    public static long calculate(
            long principalBalance,
            LocalDate dueDate,
            LocalDate asOfDate,
            int graceDays,
            int annualRateBps,
            Long capPaise) {
        if (principalBalance <= 0 || !asOfDate.isAfter(dueDate)) {
            return 0;
        }
        long daysOverdue = asOfDate.toEpochDay() - dueDate.toEpochDay();
        long chargeableDays = Math.max(0, daysOverdue - graceDays);
        if (chargeableDays == 0) {
            return 0;
        }
        BigDecimal fee = BigDecimal.valueOf(principalBalance)
                .multiply(BigDecimal.valueOf(annualRateBps))
                .multiply(BigDecimal.valueOf(chargeableDays))
                .divide(TEN_THOUSAND.multiply(DAYS_PER_YEAR), 0, RoundingMode.HALF_UP);
        long result = fee.longValueExact();
        return capPaise == null ? result : Math.min(result, capPaise);
    }
}
