 package com.recipeai.recipebackend.controller;
 import com.recipeai.recipebackend.dto.MoodRequest;
 import com.google.cloud.speech.v1.*;
 import com.google.protobuf.ByteString;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.multipart.MultipartFile;
 import java.util.stream.Collectors;



 import com.recipeai.recipebackend.service.SpeechToTextService;
import com.recipeai.recipebackend.model.KnowledgeEntry;
import com.recipeai.recipebackend.service.KnowledgeBaseService;
import com.recipeai.recipebackend.service.RecipeService;
import com.recipeai.recipebackend.service.YourAIService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow frontend access
public class AIController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final RecipeService recipeService;
    private final YourAIService yourAIService;
    private final SpeechToTextService speechToTextService;

    @Autowired
    public AIController(KnowledgeBaseService knowledgeBaseService, RecipeService recipeService,YourAIService yourAIService,SpeechToTextService speechToTextService) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.recipeService = recipeService;
        this.yourAIService = yourAIService;
        this.speechToTextService = speechToTextService;
    }
    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeEntry>> search(@RequestParam String query) {
        List<KnowledgeEntry> results =knowledgeBaseService.search(query); // return top 5
        return ResponseEntity.ok(results);
    }
    @PostMapping("/api/stt")
    public String speechToText(@RequestParam("file") MultipartFile file) throws Exception {
        byte[] audioBytes = file.getBytes();

        try (SpeechClient speechClient = SpeechClient.create()) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                .setLanguageCode("en-US")
                .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(ByteString.copyFrom(audioBytes))
                .build();

            RecognizeResponse response = speechClient.recognize(config, audio);
            return response.getResultsList().stream()
                .map(result -> result.getAlternativesList().get(0).getTranscript())
                .collect(Collectors.joining(" "));
        }
    }

  
    @PostMapping("/speech-to-text")
    public ResponseEntity<String> convertSpeechToText(@RequestParam("file") MultipartFile file) {
        File mp3File = null;
        File wavFile = null;
        try {
            // Save uploaded MP3 file temporarily
            mp3File = File.createTempFile("speech", ".mp3");
            file.transferTo(mp3File);

            // Prepare temp wav file
            wavFile = File.createTempFile("speech_converted", ".wav");

            // Build ffmpeg command to convert mp3 to wav
            String ffmpegCmd = String.format("ffmpeg -y -i %s %s", 
                    mp3File.getAbsolutePath(), wavFile.getAbsolutePath());

            // Run the ffmpeg command
            Process process = Runtime.getRuntime().exec(ffmpegCmd);

            // Wait for process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // Read error stream to log the problem
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    StringBuilder errorMsg = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        errorMsg.append(line).append("\n");
                    }
                    return ResponseEntity.status(500).body("FFmpeg conversion error: " + errorMsg.toString());
                }
            }

            // Call your STT service on the wav file
            String transcribedText = speechToTextService.transcribe(wavFile.getAbsolutePath());

            return ResponseEntity.ok(transcribedText);

        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(500).body("Error processing audio: " + e.getMessage());
        } finally {
            // Cleanup temp files
            if (mp3File != null && mp3File.exists()) mp3File.delete();
            if (wavFile != null && wavFile.exists()) wavFile.delete();
        }
    }

    @PostMapping("/mood-recipes")
    public ResponseEntity<String> getRecipesByMood(@RequestBody MoodRequest request) {
        String mood = request.getMood();
        String type = request.getType(); // veg or non-veg

        System.out.println("Received mood: " + mood);
        System.out.println("Selected type: " + type);

        // Updated prompt to include type filter
        String prompt = yourAIService.generateMoodPromptWithType(mood, type);
        String aiResponse = yourAIService.callOpenRouter(prompt);

        return ResponseEntity.ok(aiResponse);
    }

    // Endpoint: /api/ask?query=diabetic
    @GetMapping("/ask")
    public ResponseEntity<String> getAnswer(@RequestParam String query) {
        // STEP 1: Do semantic search
        List<KnowledgeEntry> matches = knowledgeBaseService.search(query)
        		.stream()
                .limit(3)
                .collect(Collectors.toList());;

        if (matches.isEmpty()) {
            return ResponseEntity.ok("Sorry, I couldn't find an answer to that question.");
        }

        // STEP 2: Combine matched contents into a single string
        String context = matches.stream()
                .map(KnowledgeEntry::getContent)
                .collect(Collectors.joining("\n"));

        // STEP 3: Construct the prompt
        String prompt = "You are a helpful health and recipe assistant.\n\n"
                      + "Context:\n" + context + "\n\n"
                      + "Question: " + query + "\n\n"
                      + "Answer:";

        // STEP 4: Call OpenRouter GPT model
        String answer = recipeService.generateFromContext(prompt);

        return ResponseEntity.ok(answer);
    }
}


