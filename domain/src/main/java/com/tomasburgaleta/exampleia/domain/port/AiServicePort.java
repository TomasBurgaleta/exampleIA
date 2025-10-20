package com.tomasburgaleta.exampleia.domain.port;

/**
 * Port interface for AI service integration.
 * This interface allows the application to communicate with different AI providers
 * by implementing this contract in the infrastructure layer.
 */
public interface AiServicePort {
    
    /**
     * Sends a text prompt to the AI service and receives a response
     * 
     * @param prompt The text prompt to send to the AI
     * @return The AI's response as a String
     * @throws AudioProcessingException if the AI service fails to process the request
     */
    String sendPrompt(String prompt) throws AudioProcessingException;
}
