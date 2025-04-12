package com.simplesignificance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String getInput(@RequestParam("csvFile")MultipartFile csvFile, Model model){
        if (csvFile == null || csvFile.isEmpty()) {
            model.addAttribute("error", "No file selected. Please input a valid CSV file to upload.");
            return "index";
        }

        // todo: Call analysisService.parse(csvFile) here later
        try (var reader = new BufferedReader(new InputStreamReader(csvFile.getInputStream()))) {
            logger.info("=== Uploaded file contents ===");
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
            logger.info("=== End of file ===");
        } catch (IOException e) {
            logger.error("Failed to read uploaded file", e);
            model.addAttribute("error", "Failed to read the uploaded file.");
            return "index";
        }

        model.addAttribute("message", "File '" + csvFile.getOriginalFilename() + "' uploaded successfully.");
        return "index";
    }
}
