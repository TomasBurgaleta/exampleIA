package com.tomasburgaleta.exampleia.application.service;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

/**
 * Application service for streaming audio operations with thread-safe byte accumulation.
 * This service manages a buffer for incoming audio data that can be safely appended
 * and extracted without concurrent access issues.
 */
public class AudioStreamingService {
    
    private final ByteArrayOutputStream audioBuffer;
    private final Object lock = new Object();
    
    public AudioStreamingService() {
        this.audioBuffer = new ByteArrayOutputStream();
    }
    
    /**
     * Adds audio bytes to the buffer in a thread-safe manner.
     * This method appends the provided bytes to the end of the existing buffer.
     * 
     * @param audioChunk The audio data chunk to append
     * @throws IllegalArgumentException if audioChunk is null or empty
     */
    public synchronized void addAudioBytes(byte[] audioChunk) {
        Objects.requireNonNull(audioChunk, "Audio chunk cannot be null");
        
        if (audioChunk.length == 0) {
            throw new IllegalArgumentException("Audio chunk cannot be empty");
        }
        
        synchronized (lock) {
            audioBuffer.write(audioChunk, 0, audioChunk.length);
        }
    }
    
    /**
     * Extracts all accumulated audio bytes and clears the buffer in a thread-safe manner.
     * This method retrieves all data currently in the buffer and then empties it.
     * The add and extract operations are mutually exclusive.
     * 
     * @return A byte array containing all accumulated audio data
     */
    public synchronized byte[] extractAndClearAudioBytes() {
        synchronized (lock) {
            byte[] data = audioBuffer.toByteArray();
            audioBuffer.reset();
            return data;
        }
    }
    
    /**
     * Gets the current size of the audio buffer without extracting or modifying it.
     * 
     * @return The number of bytes currently in the buffer
     */
    public synchronized int getBufferSize() {
        synchronized (lock) {
            return audioBuffer.size();
        }
    }
    
    /**
     * Checks if the buffer is empty.
     * 
     * @return true if the buffer contains no data, false otherwise
     */
    public synchronized boolean isEmpty() {
        synchronized (lock) {
            return audioBuffer.size() == 0;
        }
    }
    
    /**
     * Clears all data from the buffer without returning it.
     */
    public synchronized void clear() {
        synchronized (lock) {
            audioBuffer.reset();
        }
    }
}
