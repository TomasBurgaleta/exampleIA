package com.tomasburgaleta.exampleia.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Deepgram Speech Services
 */
@Component
@ConfigurationProperties(prefix = "deepgram.speech")
public class DeepgramSpeechConfig {
    
    private String apiKey;
    private String model = "nova-2"; // Default to nova-2 model
    private String language = "es"; // Default to Spanish
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public boolean isValid() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
