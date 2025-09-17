package com.recipeai.recipebackend.model;

import java.util.List;

public class KnowledgeEntry {
    private String id;
    private String title;
    private String content;
    private List<Double> vector;
    private double similarityScore;
    public double getSimilarityScore() { return similarityScore; }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<Double> getVector() { return vector; }
    public void setVector(List<Double> vector) { this.vector = vector; }

    
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
}
