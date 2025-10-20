package com.tomasburgaleta.exampleia.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for ElevenLabs Text-to-Speech service
 */
@Component
@ConfigurationProperties(prefix = "elevenlabs")
public class ElevenLabsConfig {
    
    private String apiKey;
    private String voiceId;
    private String modelId = "eleven_multilingual_v2";
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getVoiceId() {
        return voiceId;
    }
    
    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }
    
    public String getModelId() {
        return modelId;
    }
    
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    
    public boolean isValid() {
        return apiKey != null && !apiKey.trim().isEmpty() 
            && voiceId != null && !voiceId.trim().isEmpty();
    }
}
