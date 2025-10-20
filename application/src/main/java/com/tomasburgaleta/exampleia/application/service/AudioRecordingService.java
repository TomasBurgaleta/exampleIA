package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AiServicePort;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.UUID;

/**
 * Application service for audio recording operations
 * This orchestrates the domain logic by using the AudioRecordingPort
 */
public class AudioRecordingService {
    
    private final AudioRecordingPort audioRecordingPort;
    private final AudioListenerPort audioListenerPort;
    private final SilenceDetectionService silenceDetectionService;
    private final AiServicePort aiServicePort;
    
    public AudioRecordingService(AudioRecordingPort audioRecordingPort, 
                                AudioListenerPort audioListenerPort,
                                SilenceDetectionService silenceDetectionService,
                                AiServicePort aiServicePort) {
        this.audioRecordingPort = Objects.requireNonNull(audioRecordingPort, "AudioRecordingPort cannot be null");
        this.audioListenerPort = Objects.requireNonNull(audioListenerPort, "AudioListenerPort cannot be null");
        this.silenceDetectionService = silenceDetectionService;
        this.aiServicePort = aiServicePort;
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
     * Checks if the provided audio data contains silence
     * 
     * @param pcmData The PCM audio data to check
     * @param samplesPerSecond The sample rate
     * @param bitsPerSample The bit depth
     * @param channels The number of channels
     * @return true if silence is detected, false otherwise
     */
    public boolean detectSilence(byte[] pcmData, long samplesPerSecond, short bitsPerSample, short channels) {
        if (silenceDetectionService == null) {
            return false; // If no silence detection service, assume not silent
        }
        
        return silenceDetectionService.isSilent(pcmData, samplesPerSecond, bitsPerSample, channels);
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
    
    /**
     * Transcribes audio stored in memory by its ID
     * Converts PCM data to WAV format and processes it for transcription
     * 
     * @param id The unique identifier of the recording to transcribe
     * @return The audio bean with transcription result
     * @throws IllegalArgumentException if id is null or empty or recording not found
     * @throws AudioProcessingException if transcription fails
     */
    public AudioBean transcribeRecording(String id) throws AudioProcessingException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Recording ID cannot be null or empty");
        }
        
        // Get the recording from memory
        AudioBean audioBean = audioRecordingPort.getRecording(id);
        
        if (audioBean == null) {
            throw new IllegalArgumentException("Recording not found with ID: " + id);
        }
        
        // Convert PCM data to WAV format
        byte[] wavData = convertPcmToWav(
            audioBean.getAudioData(),
            audioBean.getSamplesPerSecond(),
            audioBean.getBitsPerSample(),
            audioBean.getChannels()
        );
        
        // Create a new AudioBean with WAV data for transcription
        AudioBean wavAudioBean = new AudioBean(audioBean.getId(), wavData);
        wavAudioBean.setSamplesPerSecond(audioBean.getSamplesPerSecond());
        wavAudioBean.setBitsPerSample(audioBean.getBitsPerSample());
        wavAudioBean.setChannels(audioBean.getChannels());
        
        // Process audio for transcription
        audioListenerPort.listenAudio(wavAudioBean);
        
        // Update the original audio bean with transcription
        audioBean.setTranscribedText(wavAudioBean.getTranscribedText());
        
        // Send transcribed text to AI and get response if transcription is not empty
        if (aiServicePort != null && audioBean.hasTranscribedText()) {
            try {
                String aiResponse = aiServicePort.sendPrompt(audioBean.getTranscribedText());
                audioBean.setAiResponse(aiResponse);
            } catch (AudioProcessingException e) {
                // Log but don't fail the transcription if AI fails
                // The transcription result is still valid
                throw new AudioProcessingException("Transcription successful but AI processing failed: " + e.getMessage(), e);
            }
        }
        
        return audioBean;
    }
    
    /**
     * Converts raw PCM audio data to WAV format by adding WAV header
     * 
     * @param pcmData Raw PCM audio data
     * @param sampleRate Sample rate in Hz
     * @param bitsPerSample Bits per sample (8, 16, 24, etc.)
     * @param channels Number of audio channels
     * @return Complete WAV file as byte array
     */
    private byte[] convertPcmToWav(byte[] pcmData, long sampleRate, short bitsPerSample, short channels) {
        int pcmDataSize = pcmData.length;
        int wavHeaderSize = 44;
        
        ByteBuffer buffer = ByteBuffer.allocate(wavHeaderSize + pcmDataSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // RIFF header
        buffer.put("RIFF".getBytes());
        buffer.putInt(36 + pcmDataSize); // File size - 8
        buffer.put("WAVE".getBytes());
        
        // fmt chunk
        buffer.put("fmt ".getBytes());
        buffer.putInt(16); // fmt chunk size (PCM)
        buffer.putShort((short) 1); // Audio format (1 = PCM)
        buffer.putShort(channels);
        buffer.putInt((int) sampleRate);
        buffer.putInt((int) (sampleRate * channels * bitsPerSample / 8)); // Byte rate
        buffer.putShort((short) (channels * bitsPerSample / 8)); // Block align
        buffer.putShort(bitsPerSample);
        
        // data chunk
        buffer.put("data".getBytes());
        buffer.putInt(pcmDataSize);
        buffer.put(pcmData);
        
        return buffer.array();
    }
}
