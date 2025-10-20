package com.tomasburgaleta.exampleia.web.controller;

import com.tomasburgaleta.exampleia.application.service.TextToSpeechService;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Text-to-Speech operations
 */
@RestController
@RequestMapping("/api/tts")
public class TextToSpeechController {
    
    private final TextToSpeechService textToSpeechService;
    
    public TextToSpeechController(TextToSpeechService textToSpeechService) {
        this.textToSpeechService = textToSpeechService;
    }
    
    /**
     * Converts text to speech and returns the audio file
     * 
     * @param request Request containing the text to convert
     * @return Audio file as byte array with appropriate headers
     */
    @PostMapping(value = "/synthesize", produces = "audio/mpeg")
    public ResponseEntity<?> synthesizeSpeech(@RequestBody TTSRequest request) {
        try {
            // Validate input
            if (request.getText() == null || request.getText().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Text cannot be empty");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check if service is available
            if (!textToSpeechService.isServiceAvailable()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Text-to-Speech service is not available. Please check configuration.");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
            }
            
            // Convert text to speech
            byte[] audioData = textToSpeechService.convertTextToSpeech(request.getText());
            
            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/mpeg"));
            headers.setContentLength(audioData.length);
            headers.set("Content-Disposition", "inline; filename=\"speech.mp3\"");
            
            return new ResponseEntity<>(audioData, headers, HttpStatus.OK);
            
        } catch (AudioProcessingException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to synthesize speech: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * Health check endpoint for TTS service
     * 
     * @return Service availability status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        boolean available = textToSpeechService.isServiceAvailable();
        response.put("service", "Text-to-Speech");
        response.put("available", available);
        response.put("status", available ? "UP" : "DOWN");
        
        HttpStatus status = available ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Request DTO for TTS
     */
    public static class TTSRequest {
        private String text;
        
        public String getText() {
            return text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
    }
}
