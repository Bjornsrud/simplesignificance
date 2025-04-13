package com.simplesignificance.controller;

import com.simplesignificance.model.ProjectData;
import com.simplesignificance.model.TestType;
import com.simplesignificance.model.analysis.InitialAnalysisResult;
import com.simplesignificance.model.analysis.TestRecommendation;
import com.simplesignificance.model.analysis.TestResultSummary;
import com.simplesignificance.service.AnalysisService;
import com.simplesignificance.service.CsvParserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(WebController.class)
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean // Deprecated since version 3.4.0. Replace with future alternative when available.
    private CsvParserService csvParserService;

    @MockBean
    private AnalysisService analysisService;

    @Test
    void testIndexPageLoadsSuccessfully() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testUploadValidCsvReturnsProjectAndAnalysis() throws Exception {
        String csvContent = "Group1,Group2\n1,2\n3,4\n5,6";
        MockMultipartFile file = new MockMultipartFile(
                "csvFile", "test.csv", "text/csv", csvContent.getBytes()
        );

        ProjectData projectData = new ProjectData();
        projectData.setProjectTitle("Test Project");
        projectData.setGroupData(Map.of(
                "Group1", List.of(1.0, 3.0, 5.0),
                "Group2", List.of(2.0, 4.0, 6.0)
        ));

        InitialAnalysisResult analysisResult = new InitialAnalysisResult(
                Map.of("Group1", 3, "Group2", 3),
                Map.of("Group1", 2.0, "Group2", 4.0),
                Map.of("Group1", true, "Group2", true),
                List.of(new TestRecommendation(TestType.T_TEST, true, "Example")),
                false,
                true,
                Map.of("Group1", 0.1, "Group2", -0.2)
        );

        given(csvParserService.parse(file)).willReturn(projectData);
        given(analysisService.analyze(projectData)).willReturn(analysisResult);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attributeExists("analysis"))
                .andExpect(model().attributeExists("message"));
    }

    @Test
    void testUploadInvalidCsvShowsError() throws Exception {
        String csvContent = "invalid data";
        MockMultipartFile file = new MockMultipartFile(
                "csvFile", "bad.csv", "text/csv", csvContent.getBytes()
        );

        given(csvParserService.parse(file)).willThrow(new IOException("Invalid format"));

        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeDoesNotExist("project"))
                .andExpect(model().attributeDoesNotExist("analysis"));
    }
}
