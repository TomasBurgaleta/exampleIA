package com.tomasburgaleta.exampleia.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for OpenAI
 */
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAiConfig {
    
    private String apiKey;
    private String model = "gpt-3.5-turbo";
    private Double temperature = 0.7;
    private Integer maxTokens = 150;
    private Boolean logRequests = true;
    private Boolean logResponses = true;
    
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
    
    public Double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }
    
    public Integer getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public Boolean getLogRequests() {
        return logRequests;
    }
    
    public void setLogRequests(Boolean logRequests) {
        this.logRequests = logRequests;
    }
    
    public Boolean getLogResponses() {
        return logResponses;
    }
    
    public void setLogResponses(Boolean logResponses) {
        this.logResponses = logResponses;
    }
    
    public boolean isValid() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}
