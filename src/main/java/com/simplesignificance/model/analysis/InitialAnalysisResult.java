package com.simplesignificance.model.analysis;

import java.util.List;
import java.util.Map;

public record InitialAnalysisResult(
        Map<String, Integer> groupSizes,
        Map<String, Double> variances,
        Map<String, Boolean> isNormal,
        List<TestRecommendation> recommendations,
        boolean tooFewDataPoints,
        boolean lowPowerWarning,
        Map<String, Double> skewness
) {
}