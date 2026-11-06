package com.anik.township.complaints;

import com.anik.township.domain.ComplaintUrgency;

public interface ComplaintClassifier {
    Result classify(String text);

    record Result(String category, ComplaintUrgency urgency, int confidence, String reason) {
    }
}
