package com.simplesignificance.controller;

import com.simplesignificance.model.ProjectData;
import com.simplesignificance.model.TestType;
import com.simplesignificance.model.analysis.InitialAnalysisResult;
import com.simplesignificance.model.analysis.TestResultSummary;
import com.simplesignificance.service.AnalysisService;
import com.simplesignificance.service.CsvParserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
    private final CsvParserService parserService;
    private final AnalysisService analysisService;

    private ProjectData lastUploadedProject;
    private InitialAnalysisResult lastInitialAnalysisResult;
    private TestResultSummary lastTestResult;

    private static final String FRONT_PAGE = "index";

    public WebController(CsvParserService parserService, AnalysisService analysisService) {
        this.parserService = parserService;
        this.analysisService = analysisService;
    }

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        Locale locale = RequestContextUtils.getLocale(request);
        logger.info("Incoming GET / request with lang param: {}", request.getParameter("lang"));
        logger.info("Current request locale: {}", locale);
        return FRONT_PAGE;
    }

    @PostMapping("/upload")
    public String getInput(@RequestParam("csvFile") MultipartFile csvFile,
                           @RequestParam(value = "paired", required = false) String paired,
                           Model model) {
        if (csvFile == null || csvFile.isEmpty()) {
            model.addAttribute("error", "No file selected. Please input a valid CSV file to upload.");
            return FRONT_PAGE;
        }

        try {
            ProjectData project = parserService.parse(csvFile);
            project.setPaired("true".equalsIgnoreCase(paired));

            logger.info("=== Parsed project data from file: {} ===", csvFile.getOriginalFilename());
            logger.info("Project title: {}", project.getProjectTitle());

            for (Map.Entry<String, List<Double>> entry : project.getGroupData().entrySet()) {
                logger.info("Group '{}': {}", entry.getKey(), entry.getValue());
            }

            int maxSize = project.getGroupData()
                    .values()
                    .stream()
                    .mapToInt(List::size)
                    .max()
                    .orElse(0);

            InitialAnalysisResult analysis = analysisService.analyze(project);

            logger.debug("Available variance keys: {}", analysis.variances().keySet());
            logger.debug("Group sizes keys: {}", analysis.groupSizes().keySet());

            lastUploadedProject = project;
            lastInitialAnalysisResult = analysis;
            lastTestResult = null;

            model.addAttribute("maxRows", maxSize);
            model.addAttribute("message", "File '" + csvFile.getOriginalFilename() + "' uploaded successfully.");
            model.addAttribute("project", project);
            model.addAttribute("analysis", analysis);

        } catch (IOException e) {
            logger.error("Failed to parse uploaded CSV file", e);
            model.addAttribute("error", "Failed to read the uploaded file.");
        }

        return FRONT_PAGE;
    }

    @PostMapping("/analyze")
    public String runAnalysis(@RequestParam("selectedTestType") TestType selectedTestType, Model model) {
        if (lastUploadedProject == null || lastInitialAnalysisResult == null) {
            model.addAttribute("error", "No uploaded project available for analysis.");
            return FRONT_PAGE;
        }

        lastUploadedProject.setSelectedTestType(selectedTestType);
        model.addAttribute("project", lastUploadedProject);
        model.addAttribute("analysis", lastInitialAnalysisResult);
        return FRONT_PAGE;
    }

    @PostMapping("/analyze/significance")
    public String runSignificanceTest(@RequestParam("selectedTestType") TestType selectedTestType,
                                      @RequestParam(value = "paired", required = false) String paired,
                                      Model model) {
        if (lastUploadedProject == null || lastInitialAnalysisResult == null) {
            model.addAttribute("error", "No uploaded project available for analysis.");
            return FRONT_PAGE;
        }

        lastUploadedProject.setSelectedTestType(selectedTestType);
        lastUploadedProject.setPaired("true".equalsIgnoreCase(paired));

        lastTestResult = analysisService.runTest(lastUploadedProject, lastInitialAnalysisResult);

        model.addAttribute("project", lastUploadedProject);
        model.addAttribute("analysis", lastInitialAnalysisResult);
        model.addAttribute("testResult", lastTestResult);

        return FRONT_PAGE;
    }
}
