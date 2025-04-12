package com.simplesignificance.service;

import com.simplesignificance.model.ProjectData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.*;

@Service
public class CsvParserService {

    public ProjectData parse(MultipartFile file) throws IOException {
        ProjectData project = new ProjectData();
        Map<String, List<Double>> groupData = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            String[] headers = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) {
                    continue;
                }

                String[] tokens = line.split(",");
                lineNumber++;

                if (lineNumber == 1 && tokens.length == 1) {
                    // Project title
                    project.setProjectTitle(tokens[0].trim());
                    continue;
                }

                if ((lineNumber == 1 || lineNumber == 2) && headers == null && tokens.length >= 2 && !isNumeric(tokens[0]) && !isNumeric(tokens[1])) {
                    headers = Arrays.stream(tokens).map(String::trim).toArray(String[]::new);
                    for (String header : headers) {
                        groupData.put(header, new ArrayList<>());
                    }
                    continue;
                }

                // If headers are still null, create generic ones like "Group 1", "Group 2", ...
                if (headers == null) {
                    headers = new String[tokens.length];
                    for (int i = 0; i < tokens.length; i++) {
                        headers[i] = "Group " + (i + 1);
                        groupData.put(headers[i], new ArrayList<>());
                    }
                }

                // Parse numeric values
                for (int i = 0; i < tokens.length && i < headers.length; i++) {
                    String token = tokens[i].trim();
                    if (!token.isEmpty() && isNumeric(token)) {
                        double value = Double.parseDouble(token);
                        groupData.get(headers[i]).add(value);
                    }
                }
            }
        }

        project.setGroupData(groupData);
        return project;
    }

    private boolean isNumeric(String value) {
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }
}
