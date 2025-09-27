package com.tomasburgaleta.exampleia.domain.port;

import com.tomasburgaleta.exampleia.domain.model.MIObject;

/**
 * Port (interface) for audio listening services
 * Following hexagonal architecture, this is the contract that infrastructure adapters must implement
 */
public interface AudioListenerPort {
    
    /**
     * Processes audio data and updates the MIObject with the transcribed text
     * 
     * @param miObject The object containing audio data to be processed
     * @return The audio data as byte array for further processing if needed
     * @throws AudioProcessingException if the audio cannot be processed
     */
    byte[] listenAudio(MIObject miObject) throws AudioProcessingException;
}