package com.recipeai.recipebackend.dto;

public class MoodRequest {
    private String mood;
    private String type;

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
