package com.recipeai.recipebackend.controller;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import com.recipeai.recipebackend.service.TextToSpeechService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tts")
@CrossOrigin(origins = "*") 
public class TextToSpeechController {

    private final TextToSpeechService ttsService;

    public TextToSpeechController(TextToSpeechService ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping
    public ResponseEntity<byte[]> textToSpeech(@RequestBody String text) throws IOException {
        try {
            byte[] audioData = ttsService.synthesizeSpeech(text);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=recipe.mp3")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(audioData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
