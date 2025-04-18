package com.simplesignificance.service;

import com.simplesignificance.model.ProjectData;
import com.simplesignificance.model.TestType;
import com.simplesignificance.model.analysis.InitialAnalysisResult;
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

    @Test
    void testAnalyzeRecommendsMannWhitney() {

        // Data fails normality tests due to skewness = 0 and undefined kurtosis (flat)
        List<Double> g1 = generateConstant(10.0, 20);
        List<Double> g2 = generateConstant(12.0, 20);

        Map<String, List<Double>> data = new HashMap<>();
        data.put("Control", g1);
        data.put("Treatment", g2);

        ProjectData project = new ProjectData();
        project.setProjectTitle("MannWhitney Recommended");
        project.setGroupData(data);

        InitialAnalysisResult result = analysisService.analyze(project);

        boolean mannWhitneyRecommended = result.recommendations().stream()
                .anyMatch(r -> r.testType() == TestType.MANN_WHITNEY && r.recommended());

        assertTrue(mannWhitneyRecommended);
    }

    @Test
    void testAnalyzeRecommendsTTestWhenVarianceEqual() {
        // Identical spread, shifted mean
        List<Double> g1 = Arrays.asList(10.0, 10.5, 9.5, 10.2, 10.1, 10.4, 9.9, 10.3, 10.6, 10.0,
                10.0, 10.5, 9.5, 10.2, 10.1, 10.4, 9.9, 10.3, 10.6, 10.0);

        List<Double> g2 = g1.stream().map(v -> v + 2.0).toList(); // same variance, mean shifted

        Map<String, List<Double>> data = new HashMap<>();
        data.put("Group A", g1);
        data.put("Group B", g2);

        ProjectData project = new ProjectData();
        project.setProjectTitle("Equal Variance T-test");
        project.setGroupData(data);

        InitialAnalysisResult result = analysisService.analyze(project);

        boolean tTestRecommended = result.recommendations().stream()
                .anyMatch(r -> r.testType() == TestType.T_TEST && r.recommended());

        assertTrue(tTestRecommended);
    }

    @Test
    void testAnalyzeRecommendsWelchTTestWhenVarianceDifferent() {

        // Unequal variance leads to Welch T-Test to be recommended. g1	= ~0.12, g1 = ~0.17
        List<Double> g1 = Arrays.asList(10.0, 10.5, 9.5, 10.2, 10.1, 10.4, 9.9, 10.3, 10.6, 10.0,
                10.0, 10.5, 9.5, 10.2, 10.1, 10.4, 9.9, 10.3, 10.6, 10.0);

        List<Double> g2 = Arrays.asList(12.0, 11.5, 12.5, 11.8, 12.2, 12.3, 11.7, 12.1, 12.6, 12.4,
                12.0, 11.5, 12.5, 11.8, 12.2, 12.3, 11.7, 12.1, 12.6, 12.4);

        Map<String, List<Double>> data = new HashMap<>();
        data.put("Control", g1);
        data.put("Treatment", g2);

        ProjectData project = new ProjectData();
        project.setProjectTitle("T-test Recommended");
        project.setGroupData(data);

        InitialAnalysisResult result = analysisService.analyze(project);

        boolean welchRecommended = result.recommendations().stream()
                .anyMatch(r -> r.testType() == TestType.WELCH_T_TEST && r.recommended());

        assertTrue(welchRecommended);
    }

    @Test
    void testRunTestReturnsValidResult() {
        List<Double> g1 = generateJittered(10.0, 0.2, 20);
        List<Double> g2 = generateJittered(15.0, 0.2, 20);

        Map<String, List<Double>> data = new LinkedHashMap<>();
        data.put("Group A", g1);
        data.put("Group B", g2);

        ProjectData project = new ProjectData();
        project.setProjectTitle("Run Test");
        project.setGroupData(data);
        project.setSelectedTestType(TestType.T_TEST);

        InitialAnalysisResult analysis = analysisService.analyze(project);
        TestResultSummary result = analysisService.runTest(project, analysis);

        assertNotNull(result);
        assertTrue(result.getPValue() > 0 && result.getPValue() < 0.05);
        assertTrue(result.getEffectSize() > 0.8);
        assertTrue(result.isSigAt10());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void testAnalyzeWithThreeGroupsSuggestsANOVA() {
        Map<String, List<Double>> groupData = new LinkedHashMap<>();
        groupData.put("G1", generateJittered(10.0, 0.1, 20));
        groupData.put("G2", generateJittered(12.0, 0.1, 20));
        groupData.put("G3", generateJittered(11.0, 0.1, 20));

        ProjectData project = new ProjectData();
        project.setProjectTitle("ANOVA Test");
        project.setGroupData(groupData);

        InitialAnalysisResult result = analysisService.analyze(project);

        boolean anovaRecommended = result.recommendations().stream()
                .anyMatch(r -> r.testType() == TestType.ANOVA && r.recommended());

        assertTrue(anovaRecommended);
    }

    @Test
    void testAnalyzeReturnsNoRecommendationsForInvalidData() {
        Map<String, List<Double>> data = new LinkedHashMap<>();
        data.put("X", List.of(1.0));
        data.put("Y", List.of(1.5));
        data.put("Z", List.of(2.0));

        ProjectData project = new ProjectData();
        project.setProjectTitle("Not Enough");
        project.setGroupData(data);

        InitialAnalysisResult result = analysisService.analyze(project);

        assertTrue(result.recommendations().isEmpty());
        assertTrue(result.tooFewDataPoints());
    }

    @Test
    void testAnalyzeRecommendsPairedTTestWhenPairedAndNormal() {
        // To grupper med samme fordeling og parvise observasjoner
        List<Double> g1 = Arrays.asList(10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 13.5, 12.5, 11.5, 10.5,
                10.0, 11.0, 12.0, 13.0, 14.0, 15.0, 13.5, 12.5, 11.5, 10.5);
        List<Double> g2 = g1.stream().map(v -> v + 1.0).toList(); // Samme varians, mean forskjell

        Map<String, List<Double>> data = new HashMap<>();
        data.put("Before", g1);
        data.put("After", g2);

        ProjectData project = new ProjectData();
        project.setProjectTitle("Paired Normal");
        project.setGroupData(data);
        project.setPaired(true);

        InitialAnalysisResult result = analysisService.analyze(project);

        boolean pairedTTestRecommended = result.recommendations().stream()
                .anyMatch(r -> r.testType() == TestType.PAIRED_T_TEST && r.recommended());

        assertTrue(pairedTTestRecommended, "Expected Paired T-Test to be recommended");
    }

    @Test
    void testAnalyzeRecommendsWilcoxonWhenPairedAndNotNormal() {
        // Ikke-normalfordelte data (konstant fordeling gir skjevhet og lav kurtose)
        List<Double> g1 = Collections.nCopies(20, 10.0);  // Flat, null varians
        List<Double> g2 = Collections.nCopies(20, 12.0);  // Flat, null varians

        Map<String, List<Double>> data = new HashMap<>();
        data.put("Before", g1);
        data.put("After", g2);

        ProjectData project = new ProjectData();
        project.setProjectTitle("Paired Non-Normal");
        project.setGroupData(data);
        project.setPaired(true);

        InitialAnalysisResult result = analysisService.analyze(project);

        boolean wilcoxonRecommended = result.recommendations().stream()
                .anyMatch(r -> r.testType() == TestType.WILCOXON && r.recommended());

        assertTrue(wilcoxonRecommended, "Expected Wilcoxon to be recommended");
    }

    @Test
    void testAnalyzeWithThreeGroupsAndNonNormalReturnsNone() {
        // Tre grupper med ulik fordeling, ulik varians
        List<Double> g1 = Arrays.asList(10.0, 10.0, 10.0, 10.0, 10.0); // null varians
        List<Double> g2 = Arrays.asList(10.0, 15.0, 20.0, 25.0, 30.0); // stor spredning
        List<Double> g3 = Arrays.asList(8.0, 9.0, 7.0, 8.5, 9.5);      // middels variasjon

        Map<String, List<Double>> data = new LinkedHashMap<>();
        data.put("X", g1);
        data.put("Y", g2);
        data.put("Z", g3);

        ProjectData project = new ProjectData();
        project.setProjectTitle("Three Groups Non-Normal");
        project.setGroupData(data);
        project.setPaired(false);

        InitialAnalysisResult result = analysisService.analyze(project);

        boolean onlyNonePresent = result.recommendations().size() == 1 &&
                result.recommendations().get(0).testType() == TestType.NONE;

        assertTrue(onlyNonePresent, "Expected only NONE to be recommended");
    }

    private List<Double> generateSequence(double start, double end, int count) {
        List<Double> list = new ArrayList<>();
        double step = (end - start) / (count - 1);
        for (int i = 0; i < count; i++) {
            list.add(start + step * i);
        }
        return list;
    }

    private List<Double> generateConstant(double value, int count) {
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(value);
        }
        return list;
    }

    private List<Double> generateJittered(double center, double jitter, int count) {
        Random r = new Random(42); // deterministic
        List<Double> values = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            values.add(center + (r.nextDouble() * 2 - 1) * jitter); // center ± jitter
        }
        return values;
    }
}
