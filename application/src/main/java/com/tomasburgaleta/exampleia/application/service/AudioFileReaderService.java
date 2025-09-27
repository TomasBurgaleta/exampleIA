package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import com.tomasburgaleta.exampleia.domain.port.AudioFileReaderPort;

import java.util.Objects;

/**
 * Application service for audio file reading operations
 * This orchestrates the domain logic by using the AudioFileReaderPort
 */
public class AudioFileReaderService {
    
    private final AudioFileReaderPort audioFileReaderPort;
    
    public AudioFileReaderService(AudioFileReaderPort audioFileReaderPort) {
        this.audioFileReaderPort = Objects.requireNonNull(audioFileReaderPort, "AudioFileReaderPort cannot be null");
    }
    
    /**
     * Reads a WAV file from the given file path and returns it as a byte array
     * 
     * @param filePath The path to the WAV file to read
     * @return The audio file content as byte array
     * @throws AudioFileException if the file path is null/empty or file reading fails
     * @throws IllegalArgumentException if the file path is null or empty
     */
    public byte[] readWavFile(String filePath) throws AudioFileException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        
        return audioFileReaderPort.readWavFile(filePath);
    }
}