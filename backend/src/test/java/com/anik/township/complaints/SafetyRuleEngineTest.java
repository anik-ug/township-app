package com.anik.township.complaints;

import static org.junit.jupiter.api.Assertions.*;

import com.anik.township.domain.ComplaintUrgency;
import java.util.List;
import org.junit.jupiter.api.Test;

class SafetyRuleEngineTest {
    private final SafetyRuleEngine engine = new SafetyRuleEngine(
            "fire,smoke,gas leak,gas smell,smell of gas,lift stuck,stuck in lift,stuck in the lift,"
                    + "shock,electric shock,short circuit,sparking,aag,dhuan,gas leakage,current laga,lift mein fas");

    @Test
    void everyConfiguredEnglishTermTriggersCriticalEscalation() {
        List<String> terms = List.of("fire", "smoke", "gas leak", "gas smell", "smell of gas",
                "lift stuck", "stuck in lift", "stuck in the lift", "shock", "electric shock",
                "short circuit", "sparking");

        for (String term : terms) {
            SafetyRuleEngine.Result result = engine.apply("Please help: " + term + " reported");
            assertTrue(result.escalated(), term);
            assertEquals(ComplaintUrgency.CRITICAL, result.urgency(), term);
            assertTrue(result.reason().startsWith("Safety keyword: "), term);
        }
    }

    @Test
    void hinglishTermsTrigger() {
        for (String term : List.of("aag", "dhuan", "current laga", "lift mein fas")) {
            assertTrue(engine.apply(term).escalated(), term);
        }
    }

    @Test
    void matchingIsCaseInsensitive() {
        SafetyRuleEngine.Result result = engine.apply("GAS LEAK near the kitchen");
        assertTrue(result.escalated());
        assertEquals(ComplaintUrgency.CRITICAL, result.urgency());
    }

    @Test
    void safetyWordsUseBoundaries() {
        for (String text : List.of("fireplace is damaged", "shockingly poor service", "firewall is down")) {
            assertFalse(engine.apply(text).escalated(), text);
        }
    }

    @Test
    void safetyEscalationOverridesLowClassifierResult() {
        ComplaintClassifier.Result low = new ComplaintClassifier.Result(
                "OTHER", ComplaintUrgency.LOW, 20, "unknown");
        SafetyRuleEngine.Result rule = engine.apply("There is smoke in the lobby");

        ComplaintUrgency finalUrgency = rule.escalated() ? ComplaintUrgency.CRITICAL : low.urgency();
        assertTrue(rule.escalated());
        assertEquals(ComplaintUrgency.CRITICAL, finalUrgency);
    }
}
