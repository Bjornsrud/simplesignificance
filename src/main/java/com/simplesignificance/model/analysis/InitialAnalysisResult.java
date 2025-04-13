package com.simplesignificance.model.analysis;

import java.util.List;
import java.util.Map;

public class InitialAnalysisResult {

    private Map<String, Integer> groupSizes;
    private Map<String, Double> variances;
    private Map<String, Boolean> isNormal;
    private List<TestRecommendation> recommendations;
    private boolean tooFewDataPoints;
    private boolean lowPowerWarning;

    public InitialAnalysisResult(Map<String, Integer> groupSizes,
                                 Map<String, Double> variances,
                                 Map<String, Boolean> isNormal,
                                 List<TestRecommendation> recommendations,
                                 boolean tooFewDataPoints,
                                 boolean lowPowerWarning) {
        this.groupSizes = groupSizes;
        this.variances = variances;
        this.isNormal = isNormal;
        this.recommendations = recommendations;
        this.tooFewDataPoints = tooFewDataPoints;
        this.lowPowerWarning = lowPowerWarning;
    }

    public Map<String, Integer> getGroupSizes() {
        return groupSizes;
    }

    public Map<String, Double> getVariances() {
        return variances;
    }

    public Map<String, Boolean> getIsNormal() {
        return isNormal;
    }

    public List<TestRecommendation> getRecommendations() {
        return recommendations;
    }

    public boolean isTooFewDataPoints() {
        return tooFewDataPoints;
    }

    public boolean isLowPowerWarning() {
        return lowPowerWarning;
    }
}