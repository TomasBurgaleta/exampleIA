package com.tomasburgaleta.exampleia.domain.port;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;

/**
 * Port (interface) for speech-to-text services
 * This is a common interface that can be implemented by different providers (Azure, Deepgram, etc.)
 * Following hexagonal architecture, this is the contract that infrastructure adapters must implement
 */
public interface SpeechToTextPort {
    
    /**
     * Transcribes audio data and updates the AudioBean with the transcribed text and detected language
     * 
     * @param audioBean The object containing audio data to be transcribed
     * @return The audio data as byte array for further processing if needed
     * @throws AudioProcessingException if the audio cannot be transcribed
     */
    byte[] transcribe(AudioBean audioBean) throws AudioProcessingException;
}
