package com.recipeai.recipebackend.service;

import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class SpeechToTextService {

    public String transcribe(String filePath) throws IOException {
        try (SpeechClient speechClient = SpeechClient.create()) {

            // Read the audio file into memory
            byte[] data = Files.readAllBytes(Paths.get(filePath));
            ByteString audioBytes = ByteString.copyFrom(data);

            // Configure recognition request
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16) 
                    // For WAV PCM
                    .setLanguageCode("en-US")
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioBytes)
                    .build();

            // Perform the transcription
            RecognizeResponse response = speechClient.recognize(config, audio);
            StringBuilder transcript = new StringBuilder();

            for (SpeechRecognitionResult result : response.getResultsList()) {
                transcript.append(result.getAlternativesList().get(0).getTranscript());
            }

            return transcript.toString();
        }
    }
}

