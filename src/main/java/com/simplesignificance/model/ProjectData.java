package com.simplesignificance.model;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectData {

    private String projectTitle;
    private String group1Label;
    private String group2Label;

    private List<Double> group1Values;
    private List<Double> group2Values;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    // TODO: Add field to associate this with a user (e.g., userId or User object)
    // private Long userId;

    // TODO: Add test result field when statistical test has been run
    // private TestResult result;

    private TestType selectedTestType;

    // TODO: Add field to track whether parsing/validation succeeded
    // private boolean isValid;

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getGroup1Label() {
        return group1Label;
    }

    public void setGroup1Label(String group1Label) {
        this.group1Label = group1Label;
    }

    public String getGroup2Label() {
        return group2Label;
    }

    public void setGroup2Label(String group2Label) {
        this.group2Label = group2Label;
    }

    public List<Double> getGroup1Values() {
        return group1Values;
    }

    public void setGroup1Values(List<Double> group1Values) {
        this.group1Values = group1Values;
    }

    public List<Double> getGroup2Values() {
        return group2Values;
    }

    public void setGroup2Values(List<Double> group2Values) {
        this.group2Values = group2Values;
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
}
