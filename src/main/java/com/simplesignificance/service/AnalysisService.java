package com.simplesignificance.service;

import com.simplesignificance.model.ProjectData;
import com.simplesignificance.model.TestType;
import com.simplesignificance.model.analysis.AnalysisResult;
import com.simplesignificance.model.analysis.TestRecommendation;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalysisService {

    public AnalysisResult analyze(ProjectData project) {
        Map<String, List<Double>> groupData = project.getGroupData();

        Map<String, Integer> groupSizes = new LinkedHashMap<>();
        Map<String, Double> variances = new LinkedHashMap<>();
        Map<String, Boolean> isNormal = new LinkedHashMap<>();

        boolean tooFew = false;
        boolean lowPower = false;

        for (Map.Entry<String, List<Double>> entry : groupData.entrySet()) {
            String group = entry.getKey();
            List<Double> values = entry.getValue();

            groupSizes.put(group, values.size());
            variances.put(group, calculateVariance(values));
            isNormal.put(group, checkNormalDistribution(values)); // Placeholder

            if (values.size() < 15) {
                tooFew = true;
            } else if (values.size() < 30) {
                lowPower = true;
            }
        }

        List<TestRecommendation> recommendations = recommendTests(groupData, isNormal, variances);

        return new AnalysisResult(groupSizes, variances, isNormal, recommendations, tooFew, lowPower);
    }

    private double calculateVariance(List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return values.stream().mapToDouble(v -> Math.pow(v - mean, 2)).average().orElse(0.0);
    }

    private boolean checkNormalDistribution(List<Double> values) {
        // TODO: Implement proper Shapiro-Wilk or similar test later
        return values.size() >= 30; // Simple heuristic for now
    }

    private List<TestRecommendation> recommendTests(Map<String, List<Double>> data,
                                                    Map<String, Boolean> isNormal,
                                                    Map<String, Double> variances) {
        List<TestRecommendation> result = new ArrayList<>();
        int groupCount = data.size();

        boolean allNormal = isNormal.values().stream().allMatch(Boolean::booleanValue);
        boolean equalVariance = checkEqualVariance(variances);

        if (groupCount == 2) {
            result.add(new TestRecommendation(TestType.T_TEST, allNormal && equalVariance, "For normally distributed data with equal variance"));
            result.add(new TestRecommendation(TestType.WELCH_T_TEST, allNormal && !equalVariance, "Normal, unequal variances"));
            result.add(new TestRecommendation(TestType.MANN_WHITNEY, !allNormal, "Non-normal distribution"));
        } else if (groupCount > 2) {
            result.add(new TestRecommendation(TestType.ANOVA, allNormal && equalVariance, "For 3+ groups with normal distribution and equal variance"));
        }

        return result;
    }

    private boolean checkEqualVariance(Map<String, Double> variances) {
        if (variances.size() < 2) return true;
        double max = Collections.max(variances.values());
        double min = Collections.min(variances.values());
        return (max / min) <= 2.0; // rule of thumb for equality
    }
}