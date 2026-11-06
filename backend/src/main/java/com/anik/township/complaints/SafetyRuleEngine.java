package com.anik.township.complaints;

import com.anik.township.domain.ComplaintUrgency;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SafetyRuleEngine {
    private final List<String> terms;

    public SafetyRuleEngine(@Value("${app.safety-terms:fire,smoke,gas leak,gas smell,smell of gas,lift stuck,stuck in lift,stuck in the lift,shock,electric shock,short circuit,sparking,aag,dhuan,gas leakage,current laga,lift mein fas}") String terms) {
        this.terms = List.of(terms.split("\\s*,\\s*"));
    }

    public Result apply(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (String term : terms) {
            String normalized = term.toLowerCase(Locale.ROOT).trim();
            if (Pattern.compile("(?<![\\p{L}\\p{N}])" + Pattern.quote(normalized)
                    + "(?![\\p{L}\\p{N}])").matcher(lower).find()) {
                return new Result(true, ComplaintUrgency.CRITICAL, "Safety keyword: " + term);
            }
        }
        return new Result(false, null, null);
    }

    public record Result(boolean escalated, ComplaintUrgency urgency, String reason) {
    }
}
