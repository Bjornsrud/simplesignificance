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
        // Do not expect any test to be recommended for one group
        assertTrue(result.recommendations().isEmpty());
    }
}
