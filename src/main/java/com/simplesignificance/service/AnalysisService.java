package com.simplesignificance.service;

import com.simplesignificance.model.ProjectData;
import com.simplesignificance.model.TestType;
import com.simplesignificance.model.analysis.AnalysisResult;
import com.simplesignificance.model.analysis.TestRecommendation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

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

            double variance = calculateVariance(values);
            boolean normal = checkNormalDistribution(values);

            logger.debug("Group '{}': size = {}, variance = {}, normal = {}", group, values.size(), variance, normal);

            groupSizes.put(group, values.size());
            variances.put(group, variance);
            isNormal.put(group, normal);

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
        if (values.size() < 5) {
            logger.debug("Group too small for normality test (n={}), assuming not normal", values.size());
            return false;
        }

        double[] data = values.stream().mapToDouble(Double::doubleValue).toArray();
        DescriptiveStatistics stats = new DescriptiveStatistics(data);

        double skewness = stats.getSkewness();
        double kurtosis = stats.getKurtosis();
        int n = (int) stats.getN();

        // Jarque-Bera statistic
        double jb = (n / 6.0) * (Math.pow(skewness, 2) + Math.pow(kurtosis, 2) / 4);
        double pValue = 1 - new org.apache.commons.math3.distribution.ChiSquaredDistribution(2).cumulativeProbability(jb);

        logger.debug("JB = {}, p-value = {}, skewness = {}, kurtosis = {}, n = {}", jb, pValue, skewness, kurtosis, n);

        // Skewness > 1 or < -1, Kurtosis > 3 or < -3 -> not normal
        boolean isNotNormal = Math.abs(skewness) > 1 || Math.abs(kurtosis) > 3;

        if (pValue < 0.05 || isNotNormal) {
            logger.debug("Data is not normally distributed based on skewness, kurtosis, and p-value.");
            return false;
        }

        return true; // Normal if p > 0.05, skewness and kurtosis are within limits
    }

    private boolean checkEqualVariance(Map<String, Double> variances) {
        if (variances.size() < 2) return true;
        double max = Collections.max(variances.values());
        double min = Collections.min(variances.values());
        return (max / min) <= 2.0;
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
            boolean recommendAnova = allNormal && equalVariance;
            result.add(new TestRecommendation(TestType.ANOVA, recommendAnova, "For 3+ groups with normal distribution and equal variance"));
        }

        return result;
    }
}
