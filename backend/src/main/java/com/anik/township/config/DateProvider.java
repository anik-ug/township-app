package com.anik.township.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DateProvider {

    private final Clock clock;
    private final String asOfDate;

    public DateProvider(
            Clock clock,
            @Value("${app.as-of-date:}") String asOfDate) {
        this.clock = clock;
        this.asOfDate = asOfDate;
    }

    public LocalDate asOfDate() {
        return asOfDate == null || asOfDate.isBlank()
                ? clock.instant().atZone(clock.getZone()).toLocalDate()
                : LocalDate.parse(asOfDate);
    }

    public OffsetDateTime now() {
        return clock.instant().atZone(clock.getZone()).toOffsetDateTime();
    }
}
