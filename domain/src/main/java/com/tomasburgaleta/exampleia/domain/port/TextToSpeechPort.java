package com.tomasburgaleta.exampleia.domain.port;

/**
 * Port interface for Text-to-Speech service integration.
 * This interface allows the application to convert text to speech audio
 * by implementing this contract in the infrastructure layer.
 */
public interface TextToSpeechPort {
    
    /**
     * Converts text to speech audio data
     * 
     * @param text The text to convert to speech
     * @return The audio data as byte array (typically in MP3 or other audio format)
     * @throws AudioProcessingException if the TTS service fails to process the request
     */
    byte[] synthesizeSpeech(String text) throws AudioProcessingException;
    
    /**
     * Checks if the TTS service is properly configured and available
     * 
     * @return true if the service is available, false otherwise
     */
    boolean isAvailable();
}
