package com.simplesignificance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class WebController {

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

        model.addAttribute("message", "File '" + csvFile.getOriginalFilename() + "' uploaded successfully.");
        return "index";
    }
}
