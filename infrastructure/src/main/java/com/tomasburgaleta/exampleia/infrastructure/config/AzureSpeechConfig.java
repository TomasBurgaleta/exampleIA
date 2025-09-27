package com.tomasburgaleta.exampleia.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Azure Speech Services
 */
@Component
@ConfigurationProperties(prefix = "azure.speech")
public class AzureSpeechConfig {
    
    private String subscriptionKey;
    private String region;
    private String language = "es-ES"; // Default to Spanish
    
    public String getSubscriptionKey() {
        return subscriptionKey;
    }
    
    public void setSubscriptionKey(String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public boolean isValid() {
        return subscriptionKey != null && !subscriptionKey.trim().isEmpty() &&
               region != null && !region.trim().isEmpty();
    }
}