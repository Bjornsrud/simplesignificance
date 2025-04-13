package com.simplesignificance.model.analysis;

import com.simplesignificance.model.TestType;

import java.util.List;
import java.util.Map;

public class TestResultSummary {

    private final Map<String, List<Double>> groupData;
    private final InitialAnalysisResult analysis;
    private final TestType testType;
    private final double pValue;
    private final boolean sigAt10;
    private final boolean sigAt05;
    private final boolean sigAt01;
    private final boolean sigAt001;
    private final double effectSize;
    private final Map<String, Double> skewness;

    public TestResultSummary(Map<String, List<Double>> groupData,
                             InitialAnalysisResult analysis,
                             TestType testType,
                             double pValue,
                             boolean sigAt10,
                             boolean sigAt05,
                             boolean sigAt01,
                             boolean sigAt001,
                             double effectSize,
                             Map<String, Double> skewness) {
        this.groupData = groupData;
        this.analysis = analysis;
        this.testType = testType;
        this.pValue = pValue;
        this.sigAt10 = sigAt10;
        this.sigAt05 = sigAt05;
        this.sigAt01 = sigAt01;
        this.sigAt001 = sigAt001;
        this.effectSize = effectSize;
        this.skewness = skewness;
    }

    public Map<String, List<Double>> getGroupData() {
        return groupData;
    }

    public InitialAnalysisResult getAnalysis() {
        return analysis;
    }

    public TestType getTestType() {
        return testType;
    }

    public double getPValue() {
        return pValue;
    }

    public boolean isSigAt10() {
        return sigAt10;
    }

    public boolean isSigAt05() {
        return sigAt05;
    }

    public boolean isSigAt01() {
        return sigAt01;
    }

    public boolean isSigAt001() {
        return sigAt001;
    }

    public double getEffectSize() {
        return effectSize;
    }

    public Map<String, Double> getSkewness() {
        return skewness;
    }
}
