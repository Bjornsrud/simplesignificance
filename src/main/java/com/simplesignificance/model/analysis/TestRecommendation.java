package com.simplesignificance.model.analysis;

import com.simplesignificance.model.TestType;

public class TestRecommendation {
    private TestType testType;
    private boolean recommended;
    private String reason;

    public TestRecommendation(TestType testType, boolean recommended, String reason) {
        this.testType = testType;
        this.recommended = recommended;
        this.reason = reason;
    }

    public TestType getTestType() {
        return testType;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public String getReason() {
        return reason;
    }
}