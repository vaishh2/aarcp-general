package com.recipeai.recipebackend.controller;

import com.recipeai.recipebackend.service.SpeechToTextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/speech")
public class SpeechController {

    private final SpeechToTextService speechToTextService;

    public SpeechController(SpeechToTextService speechToTextService) {
        this.speechToTextService = speechToTextService;
    }

    @PostMapping("/transcribe")
    public ResponseEntity<String> transcribeAudio(@RequestParam("file") MultipartFile file) throws IOException {
        // Save uploaded file temporarily
        File tempFile = File.createTempFile("audio", ".wav");
        file.transferTo(tempFile);

        // Transcribe
        String transcript = speechToTextService.transcribe(tempFile.getAbsolutePath());

        // Delete temp file
        tempFile.delete();

        return ResponseEntity.ok(transcript);
    }
}

