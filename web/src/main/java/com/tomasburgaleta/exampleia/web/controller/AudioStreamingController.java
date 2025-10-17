package com.tomasburgaleta.exampleia.web.controller;

import com.tomasburgaleta.exampleia.application.service.AudioListenerService;
import com.tomasburgaleta.exampleia.application.service.AudioStreamingService;
import com.tomasburgaleta.exampleia.application.service.SilenceDetectionService;
import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for real-time audio streaming and transcription
 */
@RestController
@RequestMapping("/api/stream")
public class AudioStreamingController {
    
    private final AudioStreamingService audioStreamingService;
    private final AudioListenerService audioListenerService;
    private final SilenceDetectionService silenceDetectionService;
    
    private String currentSessionId = null;
    private long sessionSampleRate = 0;
    private short sessionBitDepth = 0;
    private short sessionChannels = 0;
    private long lastChunkTimestamp = 0;
    private String latestTranscription = "";
    
    public AudioStreamingController(AudioStreamingService audioStreamingService,
                                   AudioListenerService audioListenerService,
                                   SilenceDetectionService silenceDetectionService) {
        this.audioStreamingService = audioStreamingService;
        this.audioListenerService = audioListenerService;
        this.silenceDetectionService = silenceDetectionService;
    }
    
    /**
     * Starts a new streaming session
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startSession(@RequestBody SessionStartRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Clear any previous session
            if (currentSessionId != null) {
                audioStreamingService.clear();
            }
            
            // Create new session
            currentSessionId = UUID.randomUUID().toString();
            sessionSampleRate = request.getSamplesPerSecond();
            sessionBitDepth = request.getBitsPerSample();
            sessionChannels = request.getChannels();
            lastChunkTimestamp = System.currentTimeMillis();
            latestTranscription = "";
            
            response.put("sessionId", currentSessionId);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to start session: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Receives audio chunks during recording and processes them
     */
    @PostMapping("/chunk")
    public ResponseEntity<Map<String, Object>> sendChunk(@RequestBody ChunkRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (currentSessionId == null || !currentSessionId.equals(request.getSessionId())) {
                response.put("error", "Invalid or expired session");
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            if (request.getPcmData() == null || request.getPcmData().length == 0) {
                response.put("error", "PCM data cannot be empty");
                response.put("success", false);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Add chunk to buffer
            audioStreamingService.addAudioBytes(request.getPcmData());
            lastChunkTimestamp = System.currentTimeMillis();
            
            // Check for silence in the chunk
            boolean isSilent = false;
            if (silenceDetectionService != null) {
                isSilent = silenceDetectionService.isSilent(
                    request.getPcmData(),
                    sessionSampleRate,
                    sessionBitDepth,
                    sessionChannels
                );
            }
            
            response.put("success", true);
            response.put("bufferSize", audioStreamingService.getBufferSize());
            response.put("isSilent", isSilent);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to process chunk: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Stops the streaming session, extracts audio, and sends to Azure for transcription
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, Object>> stopSession(@RequestBody SessionStopRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (currentSessionId == null || !currentSessionId.equals(request.getSessionId())) {
                response.put("error", "Invalid or expired session");
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Extract all accumulated audio
            byte[] pcmData = audioStreamingService.extractAndClearAudioBytes();
            
            if (pcmData.length == 0) {
                response.put("error", "No audio data recorded");
                response.put("success", false);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Convert PCM to WAV
            byte[] wavData = convertPcmToWav(pcmData, sessionSampleRate, sessionBitDepth, sessionChannels);
            
            // Create AudioBean for transcription
            AudioBean audioBean = new AudioBean(currentSessionId, wavData);
            audioBean.setSamplesPerSecond(sessionSampleRate);
            audioBean.setBitsPerSample(sessionBitDepth);
            audioBean.setChannels(sessionChannels);
            
            // Transcribe with Azure
            audioListenerService.listenAudio(audioBean);
            
            // Store the transcription
            latestTranscription = audioBean.getTranscribedText() != null ? audioBean.getTranscribedText() : "";
            
            response.put("success", true);
            response.put("sessionId", currentSessionId);
            response.put("transcribedText", latestTranscription);
            response.put("hasTranscription", audioBean.hasTranscribedText());
            response.put("audioSize", pcmData.length);
            
            // Clean up session
            currentSessionId = null;
            
            return ResponseEntity.ok(response);
            
        } catch (AudioProcessingException e) {
            response.put("error", "Transcription failed: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("error", "Failed to stop session: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Gets the current transcription status
     */
    @GetMapping("/transcription/{sessionId}")
    public ResponseEntity<Map<String, Object>> getTranscription(@PathVariable String sessionId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("sessionId", sessionId);
            response.put("transcribedText", latestTranscription);
            response.put("hasTranscription", !latestTranscription.isEmpty());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("error", "Failed to get transcription: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Converts raw PCM audio data to WAV format by adding WAV header
     */
    private byte[] convertPcmToWav(byte[] pcmData, long sampleRate, short bitsPerSample, short channels) {
        int pcmDataSize = pcmData.length;
        int wavHeaderSize = 44;
        
        ByteBuffer buffer = ByteBuffer.allocate(wavHeaderSize + pcmDataSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // RIFF header
        buffer.put("RIFF".getBytes());
        buffer.putInt(36 + pcmDataSize);
        buffer.put("WAVE".getBytes());
        
        // fmt chunk
        buffer.put("fmt ".getBytes());
        buffer.putInt(16);
        buffer.putShort((short) 1); // PCM format
        buffer.putShort(channels);
        buffer.putInt((int) sampleRate);
        buffer.putInt((int) (sampleRate * channels * bitsPerSample / 8));
        buffer.putShort((short) (channels * bitsPerSample / 8));
        buffer.putShort(bitsPerSample);
        
        // data chunk
        buffer.put("data".getBytes());
        buffer.putInt(pcmDataSize);
        buffer.put(pcmData);
        
        return buffer.array();
    }
    
    // Request DTOs
    public static class SessionStartRequest {
        private long samplesPerSecond;
        private short bitsPerSample;
        private short channels;
        
        public long getSamplesPerSecond() { return samplesPerSecond; }
        public void setSamplesPerSecond(long samplesPerSecond) { this.samplesPerSecond = samplesPerSecond; }
        
        public short getBitsPerSample() { return bitsPerSample; }
        public void setBitsPerSample(short bitsPerSample) { this.bitsPerSample = bitsPerSample; }
        
        public short getChannels() { return channels; }
        public void setChannels(short channels) { this.channels = channels; }
    }
    
    public static class ChunkRequest {
        private String sessionId;
        private byte[] pcmData;
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public byte[] getPcmData() { return pcmData; }
        public void setPcmData(byte[] pcmData) { this.pcmData = pcmData; }
    }
    
    public static class SessionStopRequest {
        private String sessionId;
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    }
}
