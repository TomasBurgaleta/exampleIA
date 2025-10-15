package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.port.SilenceDetectorPort;

import java.util.Objects;

/**
 * Application service for detecting silence in audio data
 */
public class SilenceDetectionService {
    
    private final SilenceDetectorPort silenceDetectorPort;
    
    public SilenceDetectionService(SilenceDetectorPort silenceDetectorPort) {
        this.silenceDetectorPort = Objects.requireNonNull(silenceDetectorPort, "SilenceDetectorPort cannot be null");
    }
    
    /**
     * Detects if the given audio contains silence
     * 
     * @param pcmData The PCM audio data
     * @param samplesPerSecond The sample rate
     * @param bitsPerSample The bit depth  
     * @param channels The number of channels
     * @return true if silence is detected, false otherwise
     */
    public boolean isSilent(byte[] pcmData, long samplesPerSecond, short bitsPerSample, short channels) {
        Objects.requireNonNull(pcmData, "PCM data cannot be null");
        
        if (pcmData.length == 0) {
            return true; // Empty audio is considered silent
        }
        
        return silenceDetectorPort.detectSilence(pcmData, samplesPerSecond, bitsPerSample, channels);
    }
}
