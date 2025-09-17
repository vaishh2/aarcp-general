package com.recipeai.recipebackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import org.json.JSONObject;
import org.json.JSONArray;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipeai.recipebackend.model.KnowledgeEntry;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class YourAIService {

    private final EmbeddingService embeddingService;
    private List<KnowledgeEntry> knowledgeEntries = new ArrayList<>();
    @Value("${openrouter.api.key}")
    private String apiKey;
    public YourAIService(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    public Collection<KnowledgeEntry> getAllKnowledgeEntries() {
        return knowledgeEntries;
    }
    public String generateMoodPrompt(String mood) {
        return switch (mood.toLowerCase()) {
            case "tired" -> "Suggest 5 easy and comforting recipes for someone who is tired. Each with a title and 1-line description.";
            case "feeling sick" -> "Suggest 5 healing, warm recipes for someone who is feeling sick. Short titles and descriptions.";
            case "want comfort food" -> "Suggest 5 comforting Indian-style recipes. Simple titles and 1-line descriptions.";
            case "need energy" -> "Suggest 5 high-protein or energy-boosting meals with short descriptions.";
            case "party mode" -> "Suggest 5 exciting party snacks or dishes with fun names and short descriptions.";
            default -> "Suggest 5 healthy recipes.";
        };
    }

    // 🔹 Step 2: API call to OpenRouter
  
    

    public String callOpenRouter(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            JSONObject requestBody = new JSONObject()
                .put("model", "cohere/command-r-plus") // or another available model
                .put("messages", new JSONArray()
                    .put(new JSONObject()
                        .put("role", "system")
                        .put("content", "You are a helpful recipe assistant. Respond with a list of recipe suggestions."))
                    .put(new JSONObject()
                        .put("role", "user")
                        .put("content", prompt)
                    )
                );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "http://localhost:3000") // ✅ required
                .header("X-Title", "recipe-ai")          
// replace with your key
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("🔍 Status: " + response.statusCode());
            System.out.println("🔍 Response Body: " + response.body());
            if (response.statusCode() != 200) {
                return "{\"error\":\"OpenRouter returned status " + response.statusCode() + "\"}";
            }
            String raw = response.body();
            JSONObject json = new JSONObject(raw);
            JSONArray choices = json.getJSONArray("choices");
            String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
            return content;
  // frontend will parse content

           } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Failed to fetch from OpenRouter: " + e.getMessage() + "\"}";
        }
    }

    public String generateMoodPromptWithType(String mood, String type) {
        String moodPart = (mood != null && !mood.isEmpty()) ? mood : "neutral";
        String typePart = (type != null && !type.isEmpty()) ? type : "any";

        return "Give me a list of 5 " + typePart + " recipes for someone who is feeling " + moodPart + ". "
             + "Make sure the recipes are simple and easy to cook.";
    }

    @PostConstruct
    public void loadKnowledgeData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = getClass().getResourceAsStream("/data/knowledge.json");

            List<KnowledgeEntry> entries = mapper.readValue(is, new TypeReference<>() {});
            this.knowledgeEntries = entries;

            System.out.println("✅ Loaded " + entries.size() + " entries from JSON.");

        } catch (Exception e) {
            System.err.println("❌ Failed to load knowledge data: " + e.getMessage());
        }
    }

	
}

