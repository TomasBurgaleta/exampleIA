package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.domain.port.TextToSpeechPort;

import java.util.Objects;

/**
 * Application service for Text-to-Speech operations
 * This service orchestrates the conversion of text to speech audio
 */
public class TextToSpeechService {
    
    private final TextToSpeechPort textToSpeechPort;
    
    public TextToSpeechService(TextToSpeechPort textToSpeechPort) {
        this.textToSpeechPort = Objects.requireNonNull(textToSpeechPort, "TextToSpeechPort cannot be null");
    }
    
    /**
     * Converts the given text to speech audio
     * 
     * @param text The text to convert
     * @return The audio data as byte array
     * @throws AudioProcessingException if conversion fails
     */
    public byte[] convertTextToSpeech(String text) throws AudioProcessingException {
        Objects.requireNonNull(text, "Text cannot be null");
        
        if (text.trim().isEmpty()) {
            throw new AudioProcessingException("Text cannot be empty");
        }
        
        if (!textToSpeechPort.isAvailable()) {
            throw new AudioProcessingException("Text-to-Speech service is not available. Please check configuration.");
        }
        
        return textToSpeechPort.synthesizeSpeech(text);
    }
    
    /**
     * Checks if the Text-to-Speech service is available
     * 
     * @return true if available, false otherwise
     */
    public boolean isServiceAvailable() {
        return textToSpeechPort.isAvailable();
    }
}
