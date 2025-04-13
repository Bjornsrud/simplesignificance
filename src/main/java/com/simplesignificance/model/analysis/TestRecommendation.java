package com.simplesignificance.model.analysis;

import com.simplesignificance.model.TestType;

public record TestRecommendation(TestType testType, boolean recommended, String reason) {
}