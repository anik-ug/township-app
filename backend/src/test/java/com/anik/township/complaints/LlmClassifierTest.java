package com.anik.township.complaints;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LlmClassifierTest {
    private KeywordClassifier fallback;
    private ClassifierHttpClient client;
    private LlmClassifier classifier;

    @BeforeEach
    void setUp() {
        fallback = new KeywordClassifier("water,leak", "power,electric", "lift,elevator",
                "security,guard", "clean,garbage", "parking,car");
        client = mock(ClassifierHttpClient.class);
        classifier = new LlmClassifier(fallback, client, new ObjectMapper(),
                "test-key", "http://classifier.test", "test-model");
    }

    @Test
    void timeoutFallsBackWithoutThrowing() {
        when(client.classify(anyString(), anyString(), anyString(), anyString()))
                .thenAnswer(invocation -> {
                    Thread.sleep(5_500);
                    return "{}";
                });

        ComplaintClassifier.Result result = classifier.classify("water leak");

        assertEquals("PLUMBING", result.category());
    }

    @Test
    void invalidJsonFallsBack() {
        when(client.classify(anyString(), anyString(), anyString(), anyString())).thenReturn("not-json");
        assertEquals("PLUMBING", classifier.classify("water leak").category());
    }

    @Test
    void unknownCategoryFallsBack() {
        when(client.classify(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("{\"category\":\"UNKNOWN\",\"urgency\":\"LOW\",\"confidence\":90,\"reason\":\"x\"}");
        assertEquals("PLUMBING", classifier.classify("water leak").category());
    }

    @Test
    void validAnswerIsReturned() {
        when(client.classify(anyString(), anyString(), anyString(), anyString()))
                .thenReturn("{\"category\":\"ELECTRICAL\",\"urgency\":\"HIGH\",\"confidence\":87,\"reason\":\"power issue\"}");

        ComplaintClassifier.Result result = classifier.classify("power issue");

        assertEquals("ELECTRICAL", result.category());
        assertEquals(com.anik.township.domain.ComplaintUrgency.HIGH, result.urgency());
        assertEquals(87, result.confidence());
        assertEquals("power issue", result.reason());
    }
}
