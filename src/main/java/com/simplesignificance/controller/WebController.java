package com.simplesignificance.controller;

import com.simplesignificance.model.ProjectData;
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

    public WebController(CsvParserService parserService) {
        this.parserService = parserService;
    }

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        logger.info("Incoming GET / request with lang param: {}", request.getParameter("lang"));
        Locale locale = RequestContextUtils.getLocale(request);
        logger.info("Current request locale: {}", locale);
        return "index";
    }
    @PostMapping("/upload")
    public String getInput(@RequestParam("csvFile") MultipartFile csvFile, Model model) {
        if (csvFile == null || csvFile.isEmpty()) {
            model.addAttribute("error", "No file selected. Please input a valid CSV file to upload.");
            return "index";
        }

        try {
            ProjectData project = parserService.parse(csvFile);

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

            model.addAttribute("maxRows", maxSize);
            model.addAttribute("message", "File '" + csvFile.getOriginalFilename() + "' uploaded successfully.");
            model.addAttribute("project", project);

        } catch (IOException e) {
            logger.error("Failed to parse uploaded CSV file", e);
            model.addAttribute("error", "Failed to read the uploaded file.");
        }

        return "index";
    }
}