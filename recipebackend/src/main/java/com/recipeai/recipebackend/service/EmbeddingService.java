package com.recipeai.recipebackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmbeddingService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    // ✅ Use OpenRouter's embedding endpoint
    private final String apiUrl = "https://openrouter.ai/api/v1/embeddings";

    public List<Double> generateEmbedding(String inputText) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "http://localhost:3000"); // Replace with your actual frontend if needed
        headers.set("X-Title", "RecipeAI"); // Optional: project name for OpenRouter usage tracking

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "cohere/command-r-plus"); // OpenRouter-specific model ID
        requestBody.put("input", inputText);
        

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = (Map<String, Object>) ((List<Object>) response.getBody().get("data")).get(0);
                return (List<Double>) data.get("embedding");
            } else {
                System.out.println("❌ Failed to fetch embedding. HTTP Status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.out.println("❌ Exception while generating embedding: " + e.getMessage());
        }

        return Collections.emptyList(); // fallback
    }
}
