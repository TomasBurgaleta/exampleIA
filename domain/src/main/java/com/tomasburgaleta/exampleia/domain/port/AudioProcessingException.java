package com.tomasburgaleta.exampleia.domain.port;

/**
 * Exception thrown when audio processing fails
 */
public class AudioProcessingException extends Exception {
    
    public AudioProcessingException(String message) {
        super(message);
    }
    
    public AudioProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}