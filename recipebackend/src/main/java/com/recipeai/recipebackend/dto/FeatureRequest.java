package com.recipeai.recipebackend.dto;

public class FeatureRequest {

    private String feature;
    public FeatureRequest() {
        // no-arg constructor required by Spring
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }
}
