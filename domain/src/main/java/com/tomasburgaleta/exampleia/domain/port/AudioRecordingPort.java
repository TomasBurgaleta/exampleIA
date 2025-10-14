package com.tomasburgaleta.exampleia.domain.port;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;

/**
 * Port for audio recording operations
 * This interface defines the contract for storing and retrieving audio recordings in memory
 */
public interface AudioRecordingPort {
    
    /**
     * Stores audio data in memory with PCM format
     * 
     * @param audioBean The audio bean containing PCM data and metadata
     * @return The stored audio bean with assigned ID
     */
    AudioBean storeRecording(AudioBean audioBean);
    
    /**
     * Retrieves a stored recording by its ID
     * 
     * @param id The unique identifier of the recording
     * @return The audio bean if found, null otherwise
     */
    AudioBean getRecording(String id);
    
    /**
     * Clears a specific recording from memory
     * 
     * @param id The unique identifier of the recording to clear
     * @return true if the recording was found and cleared, false otherwise
     */
    boolean clearRecording(String id);
}
