package com.anik.township.complaints;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.anik.township.domain.ComplaintUrgency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class KeywordClassifierTest {
    private KeywordClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new KeywordClassifier(
                "water,leak,pipe,tap,drainage,toilet",
                "power,electric,electricity,wiring,light,switch,current",
                "lift,elevator",
                "security,guard,theft,visitor,stranger",
                "clean,garbage,dirty,housekeeping",
                "parking,vehicle,car,bike");
    }

    @Test void classifiesPlumbing() { assertCategory("water leak in bathroom", "PLUMBING"); }
    @Test void classifiesElectrical() { assertCategory("power switch is broken", "ELECTRICAL"); }
    @Test void classifiesLift() { assertCategory("lift is not working", "LIFT"); }
    @Test void classifiesSecurity() { assertCategory("security guard needed", "SECURITY"); }
    @Test void classifiesHousekeeping() { assertCategory("garbage is not collected", "HOUSEKEEPING"); }
    @Test void classifiesParking() { assertCategory("car blocked my parking", "PARKING"); }

    @Test
    void unknownTextIsOtherWithLowConfidence() {
        ComplaintClassifier.Result result = classifier.classify("The garden needs a new bench");
        assertEquals("OTHER", result.category());
        assertEquals(ComplaintUrgency.LOW, result.urgency());
        org.junit.jupiter.api.Assertions.assertTrue(result.confidence() < 50);
    }

    private void assertCategory(String text, String category) {
        assertEquals(category, classifier.classify(text).category());
    }
}
