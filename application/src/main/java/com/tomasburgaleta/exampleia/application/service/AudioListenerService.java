package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.MIObject;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;

import java.util.Objects;

/**
 * Application service for audio listening operations
 * This orchestrates the domain logic by using the AudioListenerPort
 */
public class AudioListenerService {
    
    private final AudioListenerPort audioListenerPort;
    
    public AudioListenerService(AudioListenerPort audioListenerPort) {
        this.audioListenerPort = Objects.requireNonNull(audioListenerPort, "AudioListenerPort cannot be null");
    }
    
    /**
     * Processes audio contained in the MIObject and updates it with transcribed text
     * 
     * @param miObject The object containing audio data to process
     * @return The original audio data as byte array
     * @throws AudioProcessingException if the audio cannot be processed
     * @throws IllegalArgumentException if the MIObject is null or has no audio data
     */
    public byte[] listenAudio(MIObject miObject) throws AudioProcessingException {
        Objects.requireNonNull(miObject, "MIObject cannot be null");
        
        if (miObject.getAudioData() == null || miObject.getAudioData().length == 0) {
            throw new IllegalArgumentException("MIObject must contain audio data");
        }
        
        return audioListenerPort.listenAudio(miObject);
    }
}