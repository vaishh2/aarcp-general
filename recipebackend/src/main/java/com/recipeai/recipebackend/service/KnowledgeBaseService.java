package com.recipeai.recipebackend.service;
import com.recipeai.recipebackend.service.YourAIService;


import org.springframework.stereotype.Service;

import com.recipeai.recipebackend.model.KnowledgeEntry;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import com.recipeai.recipebackend.service.YourAIService;
import com.recipeai.recipebackend.service.YourAIService;

@Service
public class KnowledgeBaseService {

    private final YourAIService yourAIService;
    private final EmbeddingService embeddingService;

    public KnowledgeBaseService(YourAIService yourAIService, EmbeddingService embeddingService) {
        this.yourAIService = yourAIService;
        this.embeddingService = embeddingService;
    }

    public List<KnowledgeEntry> search(String query) {
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query);

        return ((YourAIService) yourAIService).getAllKnowledgeEntries().stream()
            .map(entry -> {
                double similarity = cosineSimilarity(queryEmbedding, entry.getVector());
                entry.setSimilarityScore(similarity); // optional: add a `similarity` field temporarily for sorting
                return entry;
            })
            .sorted(Comparator.comparingDouble(KnowledgeEntry::getSimilarityScore).reversed())
            .limit(3) // return top 3 relevant entries
            .collect(Collectors.toList());
    }

    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        double dot = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += Math.pow(v1.get(i), 2);
            norm2 += Math.pow(v2.get(i), 2);
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}



