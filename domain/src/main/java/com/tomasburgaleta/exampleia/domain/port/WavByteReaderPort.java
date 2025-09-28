package com.tomasburgaleta.exampleia.domain.port;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;

/**
 * Port (interface) for WAV byte array processing services
 * Following hexagonal architecture, this is the contract that infrastructure adapters must implement
 * to read and parse WAV audio data from byte arrays
 */
public interface WavByteReaderPort {
    
    /**
     * Processes a WAV audio byte array and extracts metadata and PCM data
     * 
     * @param wavBytes The WAV file content as byte array
     * @param id Unique identifier for the audio data
     * @return AudioBean with populated WAV metadata and PCM audio data (without header)
     * @throws AudioFileException if the byte array is not a valid WAV format
     * @throws IllegalArgumentException if wavBytes is null or empty, or id is null/empty
     */
    AudioBean processWavBytes(byte[] wavBytes, String id) throws AudioFileException;
}