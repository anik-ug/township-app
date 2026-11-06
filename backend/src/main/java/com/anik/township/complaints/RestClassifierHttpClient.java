package com.anik.township.complaints;

import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestClassifierHttpClient implements ClassifierHttpClient {
    private final RestClient client = RestClient.builder().build();

    @Override
    public String classify(String baseUrl, String model, String apiKey, String text) {
        return client.post().uri(baseUrl).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + apiKey)
                .body(java.util.Map.of("model", model, "messages", java.util.List.of(
                        java.util.Map.of("role", "user", "content",
                                "Return strict JSON with category, urgency, confidence, reason. Complaint: " + text))))
                .retrieve().body(String.class);
    }
}
