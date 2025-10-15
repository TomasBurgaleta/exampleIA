package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioInputStream;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioStreamFormat;
import com.microsoft.cognitiveservices.speech.audio.PushAudioInputStream;
import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.infrastructure.config.AzureSpeechConfig;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Azure implementation of the AudioListenerPort
 * This adapter integrates with Azure Speech Services to transcribe audio
 */
@Component
public class AzureAudioListenerAdapter implements AudioListenerPort {
    
    private final AzureSpeechConfig azureConfig;
    
    public AzureAudioListenerAdapter(AzureSpeechConfig azureConfig) {
        this.azureConfig = Objects.requireNonNull(azureConfig, "Azure config cannot be null");
    }
    
    @Override
    public byte[] listenAudio(AudioBean audioBean) throws AudioProcessingException {
        Objects.requireNonNull(audioBean, "MIObject cannot be null");
        
        if (!azureConfig.isValid()) {
            throw new AudioProcessingException("Azure Speech Services configuration is invalid. Please check subscription key and region.");
        }
        
        byte[] audioData = audioBean.getAudioData();
        if (audioData == null || audioData.length == 0) {
            throw new AudioProcessingException("Audio data is empty or null");
        }
        
        try {
            String transcribedText = transcribeAudio(audioData, audioBean.getSamplesPerSecond(), audioBean.getBitsPerSample(), audioBean.getChannels());
            audioBean.setTranscribedText(transcribedText);
            return audioData;
        } catch (Exception e) {
            throw new AudioProcessingException("Failed to transcribe audio: " + e.getMessage(), e);
        }
    }
    
    private String transcribeAudio(byte[] audioData, long samplesPerSecond, short bitsPerSample, short channels) throws AudioProcessingException {
        // Validate audio metadata
        if (samplesPerSecond <= 0) {
            throw new AudioProcessingException("Invalid samples per second: " + samplesPerSecond);
        }
        if (bitsPerSample <= 0) {
            throw new AudioProcessingException("Invalid bits per sample: " + bitsPerSample);
        }
        if (channels <= 0) {
            throw new AudioProcessingException("Invalid number of channels: " + channels);
        }
        
        try (SpeechConfig speechConfig = SpeechConfig.fromSubscription(azureConfig.getSubscriptionKey(), azureConfig.getRegion())) {
            // Configure Azure Speech Services
            speechConfig.setSpeechRecognitionLanguage(azureConfig.getLanguage());
            
            // Create audio input stream from byte array using AudioBean metadata
            AudioStreamFormat format = AudioStreamFormat.getWaveFormatPCM(samplesPerSecond, bitsPerSample, channels);
            try (PushAudioInputStream pushStream = AudioInputStream.createPushStream(format)) {
                AudioConfig audioConfig = AudioConfig.fromStreamInput(pushStream);
                // Create speech recognizer
                try (SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig)) {
                    // Write audio data to the stream
                    pushStream.write(audioData);
                    pushStream.close();

                    // Perform recognition
                    SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();

                    if (result.getReason() == ResultReason.RecognizedSpeech) {
                        return result.getText();
                    } else if (result.getReason() == ResultReason.NoMatch) {
                        return ""; // No speech found
                    } else {
                        throw new AudioProcessingException("Speech recognition failed: " + result.getReason());
                    }
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new AudioProcessingException("Speech recognition was interrupted", e);
        } catch (Exception e) {
            throw new AudioProcessingException("Failed to configure or execute speech recognition", e);
        }
    }
}