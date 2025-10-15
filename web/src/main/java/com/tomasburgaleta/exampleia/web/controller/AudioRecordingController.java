package com.tomasburgaleta.exampleia.web.controller;

import com.tomasburgaleta.exampleia.application.service.AudioRecordingService;
import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for in-memory audio recording operations
 */
@RestController
@RequestMapping("/api/recording")
public class AudioRecordingController {
    
    private final AudioRecordingService audioRecordingService;
    
    public AudioRecordingController(AudioRecordingService audioRecordingService) {
        this.audioRecordingService = audioRecordingService;
    }
    
    /**
     * Stores PCM audio data in memory
     * 
     * @param request The request containing PCM data and metadata
     * @return Response with recording ID and metadata
     */
    @PostMapping("/start")
    public ResponseEntity<Map<String, Object>> startRecording(@RequestBody RecordingRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate request
            if (request.getPcmData() == null || request.getPcmData().length == 0) {
                response.put("error", "PCM data cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Store recording
            AudioBean audioBean = audioRecordingService.startRecording(
                request.getPcmData(),
                request.getSamplesPerSecond(),
                request.getBitsPerSample(),
                request.getChannels()
            );
            
            // Build response
            response.put("id", audioBean.getId());
            response.put("samplesPerSecond", audioBean.getSamplesPerSecond());
            response.put("bitsPerSample", audioBean.getBitsPerSample());
            response.put("channels", audioBean.getChannels());
            response.put("dataSize", audioBean.getAudioData().length);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Internal server error: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Retrieves a stored recording by ID
     * 
     * @param id The recording ID
     * @return Response with recording data and metadata
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRecording(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            AudioBean audioBean = audioRecordingService.getRecording(id);
            
            if (audioBean == null) {
                response.put("error", "Recording not found");
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("id", audioBean.getId());
            response.put("samplesPerSecond", audioBean.getSamplesPerSecond());
            response.put("bitsPerSample", audioBean.getBitsPerSample());
            response.put("channels", audioBean.getChannels());
            response.put("dataSize", audioBean.getAudioData().length);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Internal server error: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Stops and clears a recording from memory
     * 
     * @param id The recording ID
     * @return Response indicating success or failure
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> stopRecording(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean cleared = audioRecordingService.stopRecording(id);
            
            if (!cleared) {
                response.put("error", "Recording not found");
                response.put("success", false);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            response.put("success", true);
            response.put("message", "Recording stopped and cleared");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("error", "Internal server error: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Transcribes audio stored in memory
     * Converts PCM data to WAV format and processes it for transcription
     * 
     * @param id The recording ID
     * @return Response with transcription result and metadata
     */
    @PostMapping("/{id}/transcribe")
    public ResponseEntity<Map<String, Object>> transcribeRecording(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            AudioBean audioBean = audioRecordingService.transcribeRecording(id);
            
            // Build response with transcription and metadata
            response.put("id", audioBean.getId());
            response.put("transcribedText", audioBean.getTranscribedText());
            response.put("hasTranscription", audioBean.hasTranscribedText());
            response.put("audioSize", audioBean.getAudioData().length);
            response.put("samplesPerSecond", audioBean.getSamplesPerSecond());
            response.put("bitsPerSample", audioBean.getBitsPerSample());
            response.put("channels", audioBean.getChannels());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        } catch (AudioProcessingException e) {
            response.put("error", "Audio processing failed: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            response.put("error", "Internal server error: " + e.getMessage());
            response.put("success", false);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Request DTO for starting a recording
     */
    public static class RecordingRequest {
        private byte[] pcmData;
        private long samplesPerSecond;
        private short bitsPerSample;
        private short channels;
        
        public byte[] getPcmData() {
            return pcmData;
        }
        
        public void setPcmData(byte[] pcmData) {
            this.pcmData = pcmData;
        }
        
        public long getSamplesPerSecond() {
            return samplesPerSecond;
        }
        
        public void setSamplesPerSecond(long samplesPerSecond) {
            this.samplesPerSecond = samplesPerSecond;
        }
        
        public short getBitsPerSample() {
            return bitsPerSample;
        }
        
        public void setBitsPerSample(short bitsPerSample) {
            this.bitsPerSample = bitsPerSample;
        }
        
        public short getChannels() {
            return channels;
        }
        
        public void setChannels(short channels) {
            this.channels = channels;
        }
    }
}
