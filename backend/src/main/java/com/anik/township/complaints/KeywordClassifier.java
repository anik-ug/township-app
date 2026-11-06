package com.anik.township.complaints;

import com.anik.township.domain.ComplaintUrgency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeywordClassifier implements ComplaintClassifier {
    private final Map<String, List<String>> keywords = new LinkedHashMap<>();

    public KeywordClassifier(
            @Value("${app.classifier.keywords.plumbing:water,leak,pipe,tap,drainage,toilet}") String plumbing,
            @Value("${app.classifier.keywords.electrical:power,electric,electricity,wiring,light,switch,current}") String electrical,
            @Value("${app.classifier.keywords.lift:lift,elevator}") String lift,
            @Value("${app.classifier.keywords.security:security,guard,theft,visitor,stranger}") String security,
            @Value("${app.classifier.keywords.housekeeping:clean,garbage,dirty,housekeeping}") String housekeeping,
            @Value("${app.classifier.keywords.parking:parking,vehicle,car,bike}") String parking) {
        keywords.put("PLUMBING", split(plumbing));
        keywords.put("ELECTRICAL", split(electrical));
        keywords.put("LIFT", split(lift));
        keywords.put("SECURITY", split(security));
        keywords.put("HOUSEKEEPING", split(housekeeping));
        keywords.put("PARKING", split(parking));
    }

    private static List<String> split(String value) {
        return List.of(value.split("\\s*,\\s*"));
    }

    @Override
    public Result classify(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        String best = "OTHER";
        int bestScore = 0;
        for (Map.Entry<String, List<String>> entry : keywords.entrySet()) {
            int score = (int) entry.getValue().stream()
                    .filter(word -> lower.contains(word.toLowerCase(Locale.ROOT))).count();
            if (score > bestScore) {
                best = entry.getKey();
                bestScore = score;
            }
        }
        ComplaintUrgency urgency = lower.matches(".*\\b(no water|not working|urgent|overflow|since 3 days)\\b.*")
                ? ComplaintUrgency.HIGH : bestScore == 0 ? ComplaintUrgency.LOW : ComplaintUrgency.MEDIUM;
        return new Result(best, urgency, bestScore == 0 ? 20 : Math.min(95, 55 + bestScore * 15),
                bestScore == 0 ? "No matching keyword" : "Matched configured keywords");
    }
}
