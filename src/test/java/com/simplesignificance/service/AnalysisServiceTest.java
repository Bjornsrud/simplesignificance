package com.simplesignificance.service;

import com.simplesignificance.model.ProjectData;
import com.simplesignificance.model.TestType;
import com.simplesignificance.model.analysis.InitialAnalysisResult;
import com.simplesignificance.model.analysis.TestRecommendation;
import com.simplesignificance.model.analysis.TestResultSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AnalysisServiceTest {

    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        analysisService = new AnalysisService();
    }

    @Test
    void testAnalyzeReturnsCorrectGroupSizesAndVariance() {
        Map<String, List<Double>> groupData = new HashMap<>();
        groupData.put("Group A", Arrays.asList(2.0, 3.0, 4.0, 5.0, 6.0));
        groupData.put("Group B", Arrays.asList(4.0, 5.0, 6.0, 7.0, 8.0));

        ProjectData project = new ProjectData();
        project.setProjectTitle("Variance Test");
        project.setGroupData(groupData);

        InitialAnalysisResult result = analysisService.analyze(project);

        assertEquals(5, result.groupSizes().get("Group A"));
        assertTrue(result.variances().get("Group B") > 0.0);
        assertTrue(result.recommendations().stream().anyMatch(r -> r.testType() != TestType.NONE));
    }

    @Test
    void testAnalyzeDetectsTooFewDataPoints() {
        Map<String, List<Double>> groupData = new HashMap<>();
        groupData.put("Tiny", Arrays.asList(1.0, 2.0)); // 2 points = too few

        ProjectData project = new ProjectData();
        project.setProjectTitle("Small Group");
        project.setGroupData(groupData);

        InitialAnalysisResult result = analysisService.analyze(project);

        assertTrue(result.tooFewDataPoints());
        assertTrue(result.recommendations().isEmpty()); // Should not get recommendations
    }

    @Test
    void testAnalyzeTriggersLowPowerWarning() {
        Map<String, List<Double>> groupData = new HashMap<>();
        groupData.put("A", generateSequence(1.0, 5.0, 10));
        groupData.put("B", generateSequence(10.0, 15.0, 10));

        ProjectData project = new ProjectData();
        project.setProjectTitle("Low Power");
        project.setGroupData(groupData);

        InitialAnalysisResult result = analysisService.analyze(project);

        assertTrue(result.tooFewDataPoints()); // Less than fifteen items
        assertFalse(result.lowPowerWarning()); // Not generated unless >=15
        assertFalse(result.recommendations().isEmpty());  // Should still be able to run test
    }


    private List<Double> generateSequence(double start, double end, int count) {
        List<Double> list = new ArrayList<>();
        double step = (end - start) / (count - 1);
        for (int i = 0; i < count; i++) {
            list.add(start + step * i);
        }
        return list;
    }
}
