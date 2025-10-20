package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.domain.port.TextToSpeechPort;
import com.tomasburgaleta.exampleia.infrastructure.config.ElevenLabsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ElevenLabs implementation of the TextToSpeechPort
 * This adapter integrates with ElevenLabs API to convert text to speech
 */
@Component
public class ElevenLabsAdapter implements TextToSpeechPort {
    
    private static final Logger logger = LoggerFactory.getLogger(ElevenLabsAdapter.class);
    private static final String ELEVENLABS_API_URL = "https://api.elevenlabs.io/v1/text-to-speech/";
    
    private final ElevenLabsConfig config;
    private final RestTemplate restTemplate;
    
    public ElevenLabsAdapter(ElevenLabsConfig config) {
        this.config = Objects.requireNonNull(config, "ElevenLabs config cannot be null");
        this.restTemplate = new RestTemplate();
        
        if (!config.isValid()) {
            logger.warn("ElevenLabs configuration is invalid. API key and voice ID are required.");
        } else {
            logger.info("ElevenLabs adapter initialized with voice ID: {}", config.getVoiceId());
        }
    }
    
    @Override
    public byte[] synthesizeSpeech(String text) throws AudioProcessingException {
        Objects.requireNonNull(text, "Text cannot be null");
        
        if (text.trim().isEmpty()) {
            throw new AudioProcessingException("Text cannot be empty");
        }
        
        if (!config.isValid()) {
            throw new AudioProcessingException("ElevenLabs configuration is invalid. Please check API key and voice ID.");
        }
        
        try {
            logger.debug("Synthesizing speech with ElevenLabs for text length: {}", text.length());
            
            // Build the API URL
            String url = ELEVENLABS_API_URL + config.getVoiceId();
            
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("xi-api-key", config.getApiKey());
            
            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);
            requestBody.put("model_id", config.getModelId());
            
            // Optional: Add voice settings for better control
            Map<String, Object> voiceSettings = new HashMap<>();
            voiceSettings.put("stability", 0.5);
            voiceSettings.put("similarity_boost", 0.75);
            requestBody.put("voice_settings", voiceSettings);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            // Make the API call
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                byte[].class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logger.debug("Successfully synthesized speech, audio size: {} bytes", response.getBody().length);
                return response.getBody();
            } else {
                throw new AudioProcessingException("ElevenLabs API returned unexpected status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Failed to synthesize speech with ElevenLabs", e);
            throw new AudioProcessingException("Failed to communicate with ElevenLabs: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isAvailable() {
        return config.isValid();
    }
}
