package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.port.AiServicePort;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.infrastructure.config.OpenAiConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * OpenAI implementation of the AiServicePort using LangChain4j
 * This adapter integrates with OpenAI API to send prompts and receive responses
 */
@Component
public class OpenAiAdapter implements AiServicePort {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenAiAdapter.class);
    
    private final OpenAiConfig openAiConfig;
    private final ChatLanguageModel chatModel;
    
    public OpenAiAdapter(OpenAiConfig openAiConfig) {
        this.openAiConfig = Objects.requireNonNull(openAiConfig, "OpenAI config cannot be null");
        
        if (!openAiConfig.isValid()) {
            logger.warn("OpenAI configuration is invalid. API key is required.");
        }
        
        // Initialize the ChatLanguageModel with configuration
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(openAiConfig.getApiKey())
                .modelName(openAiConfig.getModel())
                .temperature(openAiConfig.getTemperature())
                .maxTokens(openAiConfig.getMaxTokens())
                .logRequests(openAiConfig.getLogRequests())
                .logResponses(openAiConfig.getLogResponses())
                .build();
        
        logger.info("OpenAI adapter initialized with model: {}", openAiConfig.getModel());
    }
    
    @Override
    public String sendPrompt(String prompt) throws AudioProcessingException {
        Objects.requireNonNull(prompt, "Prompt cannot be null");
        
        if (prompt.trim().isEmpty()) {
            throw new AudioProcessingException("Prompt cannot be empty");
        }
        
        if (!openAiConfig.isValid()) {
            throw new AudioProcessingException("OpenAI configuration is invalid. Please check API key.");
        }
        
        try {
            if (openAiConfig.getLogRequests()) {
                logger.debug("Sending prompt to OpenAI: {}", prompt);
            }
            
            String response = chatModel.generate(prompt);
            
            if (openAiConfig.getLogResponses()) {
                logger.debug("Received response from OpenAI: {}", response);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Failed to get response from OpenAI", e);
            throw new AudioProcessingException("Failed to communicate with OpenAI: " + e.getMessage(), e);
        }
    }
}
