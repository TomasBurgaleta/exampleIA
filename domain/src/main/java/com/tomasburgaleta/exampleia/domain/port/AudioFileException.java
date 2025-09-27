package com.tomasburgaleta.exampleia.domain.port;

/**
 * Exception thrown when audio file processing fails
 * This is a RuntimeException as required by the issue specification
 */
public class AudioFileException extends RuntimeException {
    
    public AudioFileException(String message) {
        super(message);
    }
    
    public AudioFileException(String message, Throwable cause) {
        super(message, cause);
    }
}