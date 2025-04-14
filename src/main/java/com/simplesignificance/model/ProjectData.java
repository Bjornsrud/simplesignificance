package com.simplesignificance.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProjectData {

    private String projectTitle = "Untitled project";

    private Map<String, List<Double>> groupData = new LinkedHashMap<>();

    private LocalDateTime uploadedAt = LocalDateTime.now();

    private TestType selectedTestType;

    private boolean paired = false;

    // TODO: Add field to associate this with a user (e.g., userId or User object)
    // private Long userId;

    // TODO: Add test result field when statistical test has been run
    // private TestResult result;

    // TODO: Add field to track whether parsing/validation succeeded
    // private boolean isValid;

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public Map<String, List<Double>> getGroupData() {
        return groupData;
    }

    public void setGroupData(Map<String, List<Double>> groupData) {
        this.groupData = groupData;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public TestType getSelectedTestType() {
        return selectedTestType;
    }

    public void setSelectedTestType(TestType selectedTestType) {
        this.selectedTestType = selectedTestType;
    }

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }
}
