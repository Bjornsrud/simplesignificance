package com.simplesignificance.service;

import com.simplesignificance.model.ProjectData;
import com.simplesignificance.model.TestType;
import com.simplesignificance.model.analysis.InitialAnalysisResult;
import com.simplesignificance.model.analysis.TestRecommendation;
import com.simplesignificance.model.analysis.TestResultSummary;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisService.class);

    public InitialAnalysisResult analyze(ProjectData data) {
        Map<String, List<Double>> groupData = data.getGroupData();
        Map<String, Integer> groupSizes = new HashMap<>();
        Map<String, Double> variances = new HashMap<>();
        Map<String, Double> skewness = new HashMap<>();
        Map<String, Boolean> isNormal = new HashMap<>();

        boolean tooFewDataPoints = false;
        boolean lowPowerWarning = false;
        boolean invalidForTesting = false;

        for (Map.Entry<String, List<Double>> entry : groupData.entrySet()) {
            String groupName = entry.getKey();
            List<Double> values = entry.getValue();

            DescriptiveStatistics stats = new DescriptiveStatistics();
            values.forEach(stats::addValue);

            int n = values.size();
            groupSizes.put(groupName, n);
            variances.put(groupName, stats.getVariance());
            skewness.put(groupName, stats.getSkewness());

            if (n < 5) {
                invalidForTesting = true;
                tooFewDataPoints = true;
            } else if (n < 15) {
                tooFewDataPoints = true;
            } else if (n < 30) {
                lowPowerWarning = true;
            }

            boolean normal = Math.abs(stats.getSkewness()) < 1.0 && Math.abs(stats.getKurtosis()) < 3.5;
            isNormal.put(groupName, normal);
        }

        List<TestRecommendation> recommendations = invalidForTesting
                ? List.of()
                : recommendTests(groupData, variances, isNormal, data.isPaired());

        return new InitialAnalysisResult(groupSizes, variances, isNormal, recommendations, tooFewDataPoints, lowPowerWarning, skewness);
    }


    private List<TestRecommendation> recommendTests(Map<String, List<Double>> groupData,
                                                    Map<String, Double> variances,
                                                    Map<String, Boolean> isNormal,
                                                    boolean paired) {
        List<TestRecommendation> recommendations = new ArrayList<>();
        int groupCount = groupData.size();
        boolean allNormal = isNormal.values().stream().allMatch(Boolean::booleanValue);
        boolean equalVariance = variances.values().stream().distinct().count() == 1;

        if (groupCount == 2) {
            if (paired) {
                if (allNormal) {
                    recommendations.add(new TestRecommendation(TestType.PAIRED_T_TEST, true, "Data is paired and normal."));
                } else {
                    recommendations.add(new TestRecommendation(TestType.WILCOXON, true, "Data is paired and non-normal."));
                }
            } else {
                if (allNormal && equalVariance) {
                    recommendations.add(new TestRecommendation(TestType.T_TEST, true, "Groups appear normal and variances equal."));
                } else if (allNormal) {
                    recommendations.add(new TestRecommendation(TestType.WELCH_T_TEST, true, "Groups normal, variances unequal."));
                } else {
                    recommendations.add(new TestRecommendation(TestType.MANN_WHITNEY, true, "Groups non-normal, Mann-Whitney suggested."));
                }
            }
        } else if (groupCount > 2) {
            if (allNormal && equalVariance) {
                recommendations.add(new TestRecommendation(TestType.ANOVA, true, "More than two groups, normal and equal variances."));
            } else {
                recommendations.add(new TestRecommendation(TestType.NONE, false, "No valid test available for this data."));
            }
        }

        return recommendations;
    }

    public TestResultSummary runTest(ProjectData data, InitialAnalysisResult analysis) {
        Map<String, List<Double>> groupData = data.getGroupData();
        TestType testType = data.getSelectedTestType();

        double pValue = -1.0;
        double effectSize = 0.0;
        Map<String, Double> skewness = new HashMap<>();

        try {
            for (Map.Entry<String, List<Double>> entry : groupData.entrySet()) {
                DescriptiveStatistics stats = new DescriptiveStatistics();
                entry.getValue().forEach(stats::addValue);
                skewness.put(entry.getKey(), stats.getSkewness());
            }

            switch (testType) {
                case T_TEST -> {
                    List<List<Double>> groups = new ArrayList<>(groupData.values());
                    pValue = runTTest(groups.get(0), groups.get(1));
                    effectSize = computeCohensD(groups.get(0), groups.get(1));
                }
                case WELCH_T_TEST -> {
                    List<List<Double>> groups = new ArrayList<>(groupData.values());
                    pValue = runWelchTTest(groups.get(0), groups.get(1));
                    effectSize = computeCohensD(groups.get(0), groups.get(1));
                }
                case PAIRED_T_TEST -> {
                    List<List<Double>> groups = new ArrayList<>(groupData.values());
                    pValue = runPairedTTest(groups.get(0), groups.get(1));
                    effectSize = computeCohensD(groups.get(0), groups.get(1)); // approximate
                }
                case MANN_WHITNEY -> {
                    List<List<Double>> groups = new ArrayList<>(groupData.values());
                    pValue = runMannWhitneyTest(groups.get(0), groups.get(1));
                    effectSize = computeCohensD(groups.get(0), groups.get(1)); // approx
                }
                case WILCOXON -> {
                    List<List<Double>> groups = new ArrayList<>(groupData.values());
                    pValue = runWilcoxonTest(groups.get(0), groups.get(1));
                    effectSize = computeCohensD(groups.get(0), groups.get(1)); // approx
                }
                case ANOVA -> {
                    List<double[]> dataList = groupData.values().stream()
                            .map(list -> list.stream().mapToDouble(Double::doubleValue).toArray())
                            .collect(Collectors.toList());
                    pValue = new OneWayAnova().anovaPValue(dataList);
                    effectSize = computeEtaSquared(dataList);
                }
            }
        } catch (Exception e) {
            logger.error("Error running statistical test", e);
        }

        return new TestResultSummary(
                groupData,
                analysis,
                testType,
                pValue,
                pValue > 0 && pValue < 0.10,
                pValue > 0 && pValue < 0.05,
                pValue > 0 && pValue < 0.01,
                pValue > 0 && pValue < 0.001,
                effectSize,
                skewness,
                LocalDateTime.now()
        );
    }

    private double runTTest(List<Double> g1, List<Double> g2) {
        return new TTest().tTest(toArray(g1), toArray(g2));
    }

    private double runWelchTTest(List<Double> g1, List<Double> g2) {
        return new TTest().tTest(toArray(g1), toArray(g2));
    }

    private double runPairedTTest(List<Double> g1, List<Double> g2) {
        return new TTest().pairedTTest(toArray(g1), toArray(g2));
    }

    private double runMannWhitneyTest(List<Double> g1, List<Double> g2) {
        return new MannWhitneyUTest().mannWhitneyUTest(toArray(g1), toArray(g2));
    }

    private double runWilcoxonTest(List<Double> g1, List<Double> g2) {
        boolean exact = g1.size() <= 30;
        return new WilcoxonSignedRankTest().wilcoxonSignedRankTest(toArray(g1), toArray(g2), exact);
    }

    private double computeCohensD(List<Double> g1, List<Double> g2) {
        double mean1 = g1.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double mean2 = g2.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double var1 = StatUtils.variance(toArray(g1));
        double var2 = StatUtils.variance(toArray(g2));
        int n1 = g1.size();
        int n2 = g2.size();
        double pooledSD = Math.sqrt(((n1 - 1) * var1 + (n2 - 1) * var2) / (n1 + n2 - 2));
        return pooledSD == 0 ? 0 : Math.abs(mean1 - mean2) / pooledSD;
    }

    private double computeEtaSquared(List<double[]> groups) {
        double totalSS = 0;
        double betweenSS = 0;
        List<Double> all = groups.stream().flatMapToDouble(Arrays::stream).boxed().toList();
        double grandMean = all.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        for (double[] group : groups) {
            double groupMean = Arrays.stream(group).average().orElse(0);
            betweenSS += group.length * Math.pow(groupMean - grandMean, 2);
            for (double v : group) {
                totalSS += Math.pow(v - grandMean, 2);
            }
        }
        return totalSS == 0 ? 0 : betweenSS / totalSS;
    }

    private double[] toArray(List<Double> list) {
        return list.stream().mapToDouble(Double::doubleValue).toArray();
    }
}
