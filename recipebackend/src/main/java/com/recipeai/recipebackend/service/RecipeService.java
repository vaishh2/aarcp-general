package com.recipeai.recipebackend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RecipeService {

    @Value("${openrouter.api.key}") // This should be defined in application.properties
    private String openrouterApiKey;

    private final String apiUrl = "https://openrouter.ai/api/v1/chat/completions";

    // ✅ Used for RAG prompts (called by AIController)
    public String generateFromContext(String prompt) {
        return sendOpenRouterRequest(prompt);
    }

    // ✅ Generates dish names from ingredients
    public String generateDishIdeas(String ingredients) {
        String prompt = "Suggest 3 to 5 unique, creative dishes I can make using these ingredients: " + ingredients
                + ". Just return dish names as a list.";
        return sendOpenRouterRequest(prompt);
    }

    // ✅ With filters like 'quick', 'nutrition', '7days'
    public String generateDishIdeasWithFilter(String ingredients, String filter) {
        String prompt = "Suggest 3 to 5 dishes I can make using these ingredients: " + ingredients + ". ";

        switch (filter.toLowerCase()) {
            case "nutrition":
                prompt += "Make sure the dishes are healthy and nutrition-focused.";
                break;
            case "quick":
                prompt += "Make sure the dishes can be prepared quickly in under 10 minutes.";
                break;
            case "7days":
                prompt = "Give me a creative 7-day meal plan using these ingredients: " + ingredients + ".";
                break;
            case "protein":
                prompt += "Ensure the dishes are high in protein.";
                break;
            default:
                prompt += "Just return dish names as a list.";
                break;
        }

        return sendOpenRouterRequest(prompt);
    }

    // ✅ For full recipe generation
    public String generateRecipeForDish(String dishName) {
        String prompt = "Give me a clear, step-by-step recipe for the dish: " + dishName
                + ". The recipe should be easy to follow for a home cook.";
        return sendOpenRouterRequest(prompt);
    }

    // ✅ For remixing an existing dish
    public String remixDishIdea(String dishName) {
        String prompt = "Suggest a creative remix or fusion idea to modify this dish: " + dishName
                + ". The remix idea can add surprising ingredients, fusion of cuisines, or a new twist.";
        return sendOpenRouterRequest(prompt);
    }

    // 🔁 Common internal method to call OpenRouter API
    private String sendOpenRouterRequest(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openrouterApiKey);
        headers.set("HTTP-Referer", "http://localhost:3000"); // or your frontend domain
        headers.set("X-Title", "recipeai");
        Map<String, Object> request = new HashMap<>();
        request.put("model", "cohere/command-r-plus");
        request.put("max_tokens", 1000);// or use gpt-3.5-turbo if preferred

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));

        request.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object rawChoices = response.getBody().get("choices");

                if (rawChoices instanceof List<?> choices && !choices.isEmpty()) {
                    Map<String, Object> choice = (Map<String, Object>) choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    return (String) message.get("content");
                } else {
                    return "⚠️ No choices returned. Response: " + response.getBody();
                }
            } else {
                return "❌ Error: Unable to generate response. Status: " + response.getStatusCode()
                        + " Body: " + response.getBody();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Exception: " + e.getMessage();
            }
        }
}




