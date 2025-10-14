package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;

import java.util.Objects;
import java.util.UUID;

/**
 * Application service for audio recording operations
 * This orchestrates the domain logic by using the AudioRecordingPort
 */
public class AudioRecordingService {
    
    private final AudioRecordingPort audioRecordingPort;
    
    public AudioRecordingService(AudioRecordingPort audioRecordingPort) {
        this.audioRecordingPort = Objects.requireNonNull(audioRecordingPort, "AudioRecordingPort cannot be null");
    }
    
    /**
     * Starts a new recording session and stores the audio data in memory
     * 
     * @param pcmData The PCM audio data as byte array
     * @param samplesPerSecond The sample rate (e.g., 44100)
     * @param bitsPerSample The bit depth (e.g., 16)
     * @param channels The number of channels (e.g., 1 for mono, 2 for stereo)
     * @return The AudioBean containing the stored recording with metadata
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public AudioBean startRecording(byte[] pcmData, long samplesPerSecond, short bitsPerSample, short channels) {
        Objects.requireNonNull(pcmData, "PCM data cannot be null");
        
        if (pcmData.length == 0) {
            throw new IllegalArgumentException("PCM data cannot be empty");
        }
        
        if (samplesPerSecond <= 0) {
            throw new IllegalArgumentException("Samples per second must be positive");
        }
        
        if (bitsPerSample <= 0) {
            throw new IllegalArgumentException("Bits per sample must be positive");
        }
        
        if (channels <= 0) {
            throw new IllegalArgumentException("Channels must be positive");
        }
        
        // Generate unique ID
        String id = UUID.randomUUID().toString();
        
        // Create AudioBean with metadata
        AudioBean audioBean = new AudioBean(id, pcmData);
        audioBean.setSamplesPerSecond(samplesPerSecond);
        audioBean.setBitsPerSample(bitsPerSample);
        audioBean.setChannels(channels);
        
        // Store in memory
        return audioRecordingPort.storeRecording(audioBean);
    }
    
    /**
     * Retrieves a stored recording by its ID
     * 
     * @param id The unique identifier of the recording
     * @return The audio bean if found, null otherwise
     * @throws IllegalArgumentException if id is null or empty
     */
    public AudioBean getRecording(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Recording ID cannot be null or empty");
        }
        
        return audioRecordingPort.getRecording(id);
    }
    
    /**
     * Stops and clears a recording from memory
     * 
     * @param id The unique identifier of the recording to stop
     * @return true if the recording was found and cleared, false otherwise
     * @throws IllegalArgumentException if id is null or empty
     */
    public boolean stopRecording(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Recording ID cannot be null or empty");
        }
        
        return audioRecordingPort.clearRecording(id);
    }
}
