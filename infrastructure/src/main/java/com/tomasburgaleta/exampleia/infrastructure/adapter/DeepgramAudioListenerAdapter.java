package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.SpeechToTextPort;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.infrastructure.config.DeepgramSpeechConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Deepgram implementation of the SpeechToTextPort and AudioListenerPort
 * This adapter integrates with Deepgram API to transcribe audio using their REST API
 */
@Component
public class DeepgramAudioListenerAdapter implements SpeechToTextPort, AudioListenerPort {
    
    private static final String DEEPGRAM_API_URL = "https://api.deepgram.com/v1/listen";
    
    private final DeepgramSpeechConfig deepgramConfig;
    private final RestTemplate restTemplate;
    
    public DeepgramAudioListenerAdapter(DeepgramSpeechConfig deepgramConfig, RestTemplate restTemplate) {
        this.deepgramConfig = Objects.requireNonNull(deepgramConfig, "Deepgram config cannot be null");
        this.restTemplate = Objects.requireNonNull(restTemplate, "RestTemplate cannot be null");
    }
    
    @Override
    public byte[] listenAudio(AudioBean audioBean) throws AudioProcessingException {
        return transcribe(audioBean);
    }
    
    @Override
    public byte[] transcribe(AudioBean audioBean) throws AudioProcessingException {
        Objects.requireNonNull(audioBean, "AudioBean cannot be null");
        
        if (!deepgramConfig.isValid()) {
            throw new AudioProcessingException("Deepgram configuration is invalid. Please check API key.");
        }
        
        byte[] audioData = audioBean.getAudioData();
        if (audioData == null || audioData.length == 0) {
            throw new AudioProcessingException("Audio data is empty or null");
        }
        
        try {
            TranscriptionResult result = transcribeAudio(audioData, audioBean.getSamplesPerSecond(), 
                                                          audioBean.getBitsPerSample(), audioBean.getChannels());
            audioBean.setTranscribedText(result.text);
            audioBean.setDetectedLanguage(result.language);
            return audioData;
        } catch (Exception e) {
            throw new AudioProcessingException("Failed to transcribe audio with Deepgram: " + e.getMessage(), e);
        }
    }
    
    /**
     * Inner class to hold transcription result with language information
     */
    private static class TranscriptionResult {
        final String text;
        final String language;
        
        TranscriptionResult(String text, String language) {
            this.text = text;
            this.language = language;
        }
    }
    
    private TranscriptionResult transcribeAudio(byte[] audioData, long samplesPerSecond, 
                                                short bitsPerSample, short channels) throws AudioProcessingException {
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
        
        try {
            // Build request URL with query parameters
            String url = buildRequestUrl(samplesPerSecond, bitsPerSample, channels);
            
            // Set up headers with API key
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Authorization", "Token " + deepgramConfig.getApiKey());
            
            // Create request entity with audio data
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioData, headers);
            
            // Make API call
            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            
            // Parse response
            return parseTranscriptionResponse(response.getBody());
            
        } catch (RestClientException e) {
            throw new AudioProcessingException("Deepgram API call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new AudioProcessingException("Failed to process Deepgram response: " + e.getMessage(), e);
        }
    }
    
    private String buildRequestUrl(long samplesPerSecond, short bitsPerSample, short channels) {
        StringBuilder urlBuilder = new StringBuilder(DEEPGRAM_API_URL);
        urlBuilder.append("?model=").append(deepgramConfig.getModel());
        urlBuilder.append("&language=").append(deepgramConfig.getLanguage());
        urlBuilder.append("&detect_language=true"); // Enable language detection
        urlBuilder.append("&punctuate=true");
        urlBuilder.append("&encoding=linear16");
        urlBuilder.append("&sample_rate=").append(samplesPerSecond);
        urlBuilder.append("&channels=").append(channels);
        
        return urlBuilder.toString();
    }
    
    private TranscriptionResult parseTranscriptionResponse(Map<String, Object> responseBody) throws AudioProcessingException {
        if (responseBody == null) {
            throw new AudioProcessingException("Deepgram response is null");
        }
        
        try {
            // Navigate through the Deepgram response structure
            @SuppressWarnings("unchecked")
            Map<String, Object> results = (Map<String, Object>) responseBody.get("results");
            if (results == null) {
                return new TranscriptionResult("", deepgramConfig.getLanguage());
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> channels = (List<Map<String, Object>>) results.get("channels");
            if (channels == null || channels.isEmpty()) {
                return new TranscriptionResult("", deepgramConfig.getLanguage());
            }
            
            Map<String, Object> channel = channels.get(0);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> alternatives = (List<Map<String, Object>>) channel.get("alternatives");
            if (alternatives == null || alternatives.isEmpty()) {
                return new TranscriptionResult("", deepgramConfig.getLanguage());
            }
            
            Map<String, Object> alternative = alternatives.get(0);
            String transcript = (String) alternative.get("transcript");
            
            // Get detected language if available
            String detectedLanguage = deepgramConfig.getLanguage();
            if (results.containsKey("language")) {
                detectedLanguage = (String) results.get("language");
            } else if (channel.containsKey("detected_language")) {
                detectedLanguage = (String) channel.get("detected_language");
            }
            
            return new TranscriptionResult(
                transcript != null ? transcript : "",
                detectedLanguage
            );
            
        } catch (ClassCastException | NullPointerException e) {
            throw new AudioProcessingException("Failed to parse Deepgram response structure", e);
        }
    }
}
