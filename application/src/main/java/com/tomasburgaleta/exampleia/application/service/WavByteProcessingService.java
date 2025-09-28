package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import com.tomasburgaleta.exampleia.domain.port.WavByteReaderPort;

import java.util.Objects;

/**
 * Application service for processing WAV byte arrays
 * This orchestrates the domain logic by using the WavByteReaderPort
 */
public class WavByteProcessingService {
    
    private final WavByteReaderPort wavByteReaderPort;
    
    public WavByteProcessingService(WavByteReaderPort wavByteReaderPort) {
        this.wavByteReaderPort = Objects.requireNonNull(wavByteReaderPort, "WavByteReaderPort cannot be null");
    }
    
    /**
     * Processes WAV audio data from a byte array and returns an AudioBean
     * with extracted metadata and PCM data
     * 
     * @param wavBytes The WAV file content as byte array
     * @param id Unique identifier for the audio data
     * @return AudioBean containing WAV metadata and PCM audio data without header
     * @throws AudioFileException if the byte array is not a valid WAV format
     * @throws IllegalArgumentException if parameters are null or empty
     */
    public AudioBean processWavBytes(byte[] wavBytes, String id) throws AudioFileException {
        if (wavBytes == null || wavBytes.length == 0) {
            throw new IllegalArgumentException("WAV bytes cannot be null or empty");
        }
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        
        return wavByteReaderPort.processWavBytes(wavBytes, id);
    }
}