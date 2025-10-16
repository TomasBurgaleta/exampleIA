package com.tomasburgaleta.exampleia.domain.port;

/**
 * Port for silence detection in audio data
 */
public interface SilenceDetectorPort {
    
    /**
     * Detects if the given audio data contains silence
     * 
     * @param pcmData The PCM audio data to analyze
     * @param samplesPerSecond The sample rate
     * @param bitsPerSample The bit depth
     * @param channels The number of channels
     * @return true if silence is detected, false otherwise
     */
    boolean detectSilence(byte[] pcmData, long samplesPerSecond, short bitsPerSample, short channels);
}
