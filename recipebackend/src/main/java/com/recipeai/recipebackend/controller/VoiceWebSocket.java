package com.recipeai.recipebackend.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.nio.ByteBuffer;

@Component
public class VoiceWebSocket extends BinaryWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Voice WebSocket connected: " + session.getId());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        ByteBuffer payload = message.getPayload();
        byte[] audioChunk = new byte[payload.remaining()];
        payload.get(audioChunk);

        // Send this chunk to your STT (Google, Whisper, Vosk, etc.)
        // Here, just a pseudo-method detecting commands:
        String command = detectCommand(audioChunk);

        if (command != null) {
            // Send only the command to frontend
            session.sendMessage(new TextMessage(command));
            System.out.println("Detected command: " + command);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Voice WebSocket closed: " + session.getId());
    }

    // ------------------------
    // Pseudo STT command detection
    // Replace this with actual streaming STT logic
    private String detectCommand(byte[] audioChunk) {
        // Example: send audio chunk to Whisper/Google STT/DeepSpeech
        // Then check if transcript contains keywords
        String transcript = mockTranscription(audioChunk); // replace with real STT

        transcript = transcript.toLowerCase();
        if (transcript.contains("stop")) return "STOP";
        if (transcript.contains("resume")) return "RESUME";
        if (transcript.contains("start over")) return "RESTART";
        return null; // no command detected
    }

    private String mockTranscription(byte[] audioChunk) {
        // TODO: replace with real STT API call
        return "";
    }
}
