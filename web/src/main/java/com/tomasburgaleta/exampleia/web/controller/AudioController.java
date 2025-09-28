package com.tomasburgaleta.exampleia.web.controller;

import com.tomasburgaleta.exampleia.application.service.AudioListenerService;
import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for audio processing
 */
@RestController
@RequestMapping("/api/audio")
public class AudioController {
    
    private final AudioListenerService audioListenerService;
    
    public AudioController(AudioListenerService audioListenerService) {
        this.audioListenerService = audioListenerService;
    }
    
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> transcribeAudio(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate input
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if it's a WAV file (basic validation)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("audio/wav")) {
                response.put("error", "Only WAV files are supported");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create MIObject and process
            byte[] audioData = file.getBytes();
            String objectId = UUID.randomUUID().toString();
            AudioBean audioBean = new AudioBean(objectId, audioData);
            
            // Process audio
            byte[] processedAudio = audioListenerService.listenAudio(audioBean);
            
            // Build response
            response.put("id", audioBean.getId());
            response.put("transcribedText", audioBean.getTranscribedText());
            response.put("audioSize", processedAudio.length);
            response.put("hasTranscription", audioBean.hasTranscribedText());
            
            return ResponseEntity.ok(response);
            
        } catch (AudioProcessingException e) {
            response.put("error", "Audio processing failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (IOException e) {
            response.put("error", "Failed to read audio file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Azure Audio Listening Service");
        return ResponseEntity.ok(response);
    }
}