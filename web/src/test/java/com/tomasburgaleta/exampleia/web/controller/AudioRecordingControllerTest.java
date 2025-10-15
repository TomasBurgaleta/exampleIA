package com.tomasburgaleta.exampleia.web.controller;

import com.tomasburgaleta.exampleia.application.service.AudioRecordingService;
import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioRecordingControllerTest {
    
    @Mock
    private AudioRecordingService audioRecordingService;
    
    @InjectMocks
    private AudioRecordingController audioRecordingController;
    
    @Test
    void testTranscribeRecording_Success() throws AudioProcessingException {
        // Arrange
        String recordingId = "test-recording-id";
        byte[] pcmData = new byte[]{1, 2, 3, 4, 5};
        
        AudioBean audioBean = new AudioBean(recordingId, pcmData);
        audioBean.setSamplesPerSecond(44100);
        audioBean.setBitsPerSample((short) 16);
        audioBean.setChannels((short) 2);
        audioBean.setTranscribedText("Test transcription");
        
        when(audioRecordingService.transcribeRecording(recordingId))
            .thenReturn(audioBean);
        
        // Act
        ResponseEntity<Map<String, Object>> response = audioRecordingController.transcribeRecording(recordingId);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals(recordingId, body.get("id"));
        assertEquals("Test transcription", body.get("transcribedText"));
        assertTrue((Boolean) body.get("hasTranscription"));
        assertEquals(44100L, body.get("samplesPerSecond"));
        assertEquals((short) 16, body.get("bitsPerSample"));
        assertEquals((short) 2, body.get("channels"));
        
        verify(audioRecordingService, times(1)).transcribeRecording(recordingId);
    }
    
    @Test
    void testTranscribeRecording_RecordingNotFound() throws AudioProcessingException {
        // Arrange
        String recordingId = "non-existent-id";
        
        when(audioRecordingService.transcribeRecording(recordingId))
            .thenThrow(new IllegalArgumentException("Recording not found with ID: " + recordingId));
        
        // Act
        ResponseEntity<Map<String, Object>> response = audioRecordingController.transcribeRecording(recordingId);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertTrue(body.get("error").toString().contains("Recording not found"));
        
        verify(audioRecordingService, times(1)).transcribeRecording(recordingId);
    }
    
    @Test
    void testTranscribeRecording_ProcessingError() throws AudioProcessingException {
        // Arrange
        String recordingId = "test-recording-id";
        
        when(audioRecordingService.transcribeRecording(recordingId))
            .thenThrow(new AudioProcessingException("Failed to process audio"));
        
        // Act
        ResponseEntity<Map<String, Object>> response = audioRecordingController.transcribeRecording(recordingId);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertTrue(body.get("error").toString().contains("Audio processing failed"));
        
        verify(audioRecordingService, times(1)).transcribeRecording(recordingId);
    }
    
    @Test
    void testTranscribeRecording_NullId() throws AudioProcessingException {
        // Arrange
        when(audioRecordingService.transcribeRecording(null))
            .thenThrow(new IllegalArgumentException("Recording ID cannot be null or empty"));
        
        // Act
        ResponseEntity<Map<String, Object>> response = audioRecordingController.transcribeRecording(null);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertFalse((Boolean) body.get("success"));
        
        verify(audioRecordingService, times(1)).transcribeRecording(null);
    }
}
