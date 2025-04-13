package com.simplesignificance.service;

import com.simplesignificance.model.ProjectData;
import com.simplesignificance.model.TestType;
import com.simplesignificance.model.analysis.InitialAnalysisResult;
import com.simplesignificance.model.analysis.TestRecommendation;
import com.simplesignificance.model.analysis.TestResultSummary;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    public AnalysisService() {}

    public InitialAnalysisResult analyze(ProjectData data) {
        Map<String, List<Double>> groupData = data.getGroupData();
        Map<String, Integer> groupSizes = new HashMap<>();
        Map<String, Double> variances = new HashMap<>();
        Map<String, Boolean> isNormal = new HashMap<>();

        boolean tooFewDataPoints = false;
        boolean lowPowerWarning = false;

        for (Map.Entry<String, List<Double>> entry : groupData.entrySet()) {
            String groupName = entry.getKey();
            List<Double> values = entry.getValue();

            DescriptiveStatistics stats = new DescriptiveStatistics();
            values.forEach(stats::addValue);

            int n = values.size();
            groupSizes.put(groupName, n);
            variances.put(groupName, stats.getVariance());

            if (n < 15) {
                tooFewDataPoints = true;
            } else if (n < 30) {
                lowPowerWarning = true;
            }

            // Heuristic normality check using skewness and kurtosis
            boolean normal = Math.abs(stats.getSkewness()) < 1.0 && Math.abs(stats.getKurtosis()) < 3.5;
            isNormal.put(groupName, normal);
        }

        List<TestRecommendation> recommendations = recommendTests(groupData, variances, isNormal, groupSizes);

        return new InitialAnalysisResult(groupSizes, variances, isNormal, recommendations, tooFewDataPoints, lowPowerWarning);
    }

    private List<TestRecommendation> recommendTests(Map<String, List<Double>> groupData,
                                                    Map<String, Double> variances,
                                                    Map<String, Boolean> isNormal,
                                                    Map<String, Integer> groupSizes) {
        List<TestRecommendation> recommendations = new ArrayList<>();
        int groupCount = groupData.size();
        boolean allNormal = isNormal.values().stream().allMatch(b -> b);
        boolean equalSize = groupSizes.values().stream().distinct().count() == 1;
        boolean equalVariance = variances.values().stream().distinct().count() == 1;

        if (groupCount == 2) {
            if (allNormal && equalVariance) {
                recommendations.add(new TestRecommendation(TestType.T_TEST, true, "Groups appear normal and variances equal."));
            } else if (allNormal) {
                recommendations.add(new TestRecommendation(TestType.WELCH_T_TEST, true, "Groups normal, variances unequal."));
            } else {
                recommendations.add(new TestRecommendation(TestType.MANN_WHITNEY, true, "Groups non-normal, Mann-Whitney suggested."));
            }
        } else if (groupCount > 2) {
            if (allNormal && equalVariance) {
                recommendations.add(new TestRecommendation(TestType.ANOVA, true, "More than two groups, normal and equal variances."));
            } else {
                recommendations.add(new TestRecommendation(TestType.NONE, true, "Non-normal or unequal variances in >2 groups."));
            }
        }

        return recommendations;
    }

    public TestResultSummary runTest(ProjectData data, InitialAnalysisResult analysis) {
        Map<String, List<Double>> groupData = data.getGroupData();
        int groupCount = groupData.size();
        TestType testType = data.getSelectedTestType();

        double pValue = -1.0;
        boolean significantAt05 = false;
        boolean significantAt01 = false;

        try {
            switch (testType) {
                case T_TEST:
                case WELCH_T_TEST:
                case PAIRED_T_TEST:
                case MANN_WHITNEY:
                case WILCOXON:
                    if (groupCount != 2) {
                        logger.warn("Test {} is only valid for two groups, but {} groups were provided.", testType, groupCount);
                        throw new IllegalArgumentException("Selected test requires exactly 2 groups");
                    }
                    break;
            }

            switch (testType) {
                case T_TEST:
                    pValue = runTTest(groupData);
                    break;
                case WELCH_T_TEST:
                    pValue = runWelchTTest(groupData);
                    break;
                case PAIRED_T_TEST:
                    pValue = runPairedTTest(groupData);
                    break;
                case MANN_WHITNEY:
                    pValue = runMannWhitneyTest(groupData);
                    break;
                case WILCOXON:
                    pValue = runWilcoxonTest(groupData);
                    break;
                case ANOVA:
                    pValue = runAnovaTest(groupData);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error running statistical test", e);
        }

        if (pValue > 0) {
            significantAt05 = pValue < 0.05;
            significantAt01 = pValue < 0.01;
        }

        return new TestResultSummary(groupData, analysis, testType, pValue, significantAt05, significantAt01);
    }

    private double runTTest(Map<String, List<Double>> groupData) {
        Iterator<List<Double>> it = groupData.values().iterator();
        double[] g1 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        double[] g2 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        return new TTest().tTest(g1, g2);
    }

    private double runWelchTTest(Map<String, List<Double>> groupData) {
        Iterator<List<Double>> it = groupData.values().iterator();
        double[] g1 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        double[] g2 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        return new TTest().tTest(g1, g2);
    }

    private double runPairedTTest(Map<String, List<Double>> groupData) {
        Iterator<List<Double>> it = groupData.values().iterator();
        double[] g1 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        double[] g2 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        return new TTest().pairedTTest(g1, g2);
    }

    private double runMannWhitneyTest(Map<String, List<Double>> groupData) {
        Iterator<List<Double>> it = groupData.values().iterator();
        double[] g1 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        double[] g2 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        return new MannWhitneyUTest().mannWhitneyUTest(g1, g2);
    }

    private double runWilcoxonTest(Map<String, List<Double>> groupData) {
        Iterator<List<Double>> it = groupData.values().iterator();
        double[] g1 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        double[] g2 = it.next().stream().mapToDouble(Double::doubleValue).toArray();
        boolean exact = g1.length <= 30;
        return new WilcoxonSignedRankTest().wilcoxonSignedRankTest(g1, g2, exact);
    }

    private double runAnovaTest(Map<String, List<Double>> groupData) {
        List<double[]> dataList = groupData.values().stream()
                .map(list -> list.stream().mapToDouble(Double::doubleValue).toArray())
                .collect(Collectors.toList());
        return new OneWayAnova().anovaPValue(dataList);
    }
}