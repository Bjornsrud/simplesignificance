package com.simplesignificance.model.analysis;

import com.simplesignificance.model.TestType;

import java.util.List;
import java.util.Map;

public class TestResultSummary {

    private Map<String, List<Double>> groupData;
    private InitialAnalysisResult analysis;
    private TestType testType;
    private double pValue;
    private boolean significantAt05;
    private boolean significantAt01;

    public TestResultSummary(Map<String, List<Double>> groupData,
                             InitialAnalysisResult analysis,
                             TestType testType,
                             double pValue,
                             boolean significantAt05,
                             boolean significantAt01) {
        this.groupData = groupData;
        this.analysis = analysis;
        this.testType = testType;
        this.pValue = pValue;
        this.significantAt05 = significantAt05;
        this.significantAt01 = significantAt01;
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

    public double getpValue() {
        return pValue;
    }

    public boolean isSignificantAt05() {
        return significantAt05;
    }

    public boolean isSignificantAt01() {
        return significantAt01;
    }
}
