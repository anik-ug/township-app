package com.anik.township.complaints;

import com.anik.township.domain.ComplaintUrgency;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LlmClassifier implements ComplaintClassifier {
    private static final Set<String> CATEGORIES = Set.of(
            "PLUMBING", "ELECTRICAL", "LIFT", "SECURITY", "HOUSEKEEPING", "PARKING", "OTHER");
    private final KeywordClassifier fallback;
    private final ClassifierHttpClient client;
    private final ObjectMapper mapper;
    private final String key;
    private final String baseUrl;
    private final String model;

    public LlmClassifier(KeywordClassifier fallback, ClassifierHttpClient client, ObjectMapper mapper,
            @Value("${app.classifier-api-key:}") String key,
            @Value("${app.classifier.base-url}") String baseUrl,
            @Value("${app.classifier.model}") String model) {
        this.fallback = fallback;
        this.client = client;
        this.mapper = mapper;
        this.key = key;
        this.baseUrl = baseUrl;
        this.model = model;
    }

    @Override
    public Result classify(String text) {
        if (key == null || key.isBlank()) return fallback.classify(text);
        try {
            String raw = java.util.concurrent.CompletableFuture.supplyAsync(
                    () -> client.classify(baseUrl, model, key, text))
                    .get(5, TimeUnit.SECONDS);
            JsonNode n = mapper.readTree(raw);
            String category = n.path("category").asText();
            ComplaintUrgency urgency = ComplaintUrgency.valueOf(n.path("urgency").asText());
            int confidence = n.path("confidence").asInt(-1);
            String reason = n.path("reason").asText("");
            if (!CATEGORIES.contains(category) || confidence < 0 || confidence > 100 || reason.isBlank()) {
                throw new IllegalArgumentException("Invalid classifier response");
            }
            return new Result(category, urgency, confidence, reason);
        } catch (Exception ignored) {
            return fallback.classify(text);
        }
    }

    public boolean usesLlm() {
        return key != null && !key.isBlank();
    }
}
