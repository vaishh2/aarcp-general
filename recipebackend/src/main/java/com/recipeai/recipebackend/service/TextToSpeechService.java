package com.recipeai.recipebackend.service;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class TextToSpeechService {

    public byte[] synthesizeSpeech(String text) throws IOException {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

            // Input text
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Voice settings
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("en-US")
                    .setSsmlGender(SsmlVoiceGender.FEMALE) // or MALE/NEUTRAL
                    .build();

            // Audio format
            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3) // can also be LINEAR16 (WAV)
                    .build();

            // Synthesize
            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            ByteString audioContents = response.getAudioContent();

            return audioContents.toByteArray();
        }
    }
}
