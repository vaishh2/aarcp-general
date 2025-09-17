package com.recipeai.recipebackend.config;

import com.recipeai.recipebackend.controller.VoiceWebSocket;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    private final VoiceWebSocket voiceWebSocket;

    public WebSocketConfig(VoiceWebSocket voiceWebSocket) {
        this.voiceWebSocket = voiceWebSocket;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(voiceWebSocket, "/voice")
                .setAllowedOrigins("*");
    }
}
