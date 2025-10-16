package com.tomasburgaleta.exampleia.web.controller;

import com.tomasburgaleta.exampleia.application.service.AudioListenerService;
import com.tomasburgaleta.exampleia.application.service.AudioStreamingService;
import com.tomasburgaleta.exampleia.application.service.SilenceDetectionService;
import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AudioStreamingControllerTest {
    
    private AudioStreamingController controller;
    
    @Mock
    private AudioStreamingService audioStreamingService;
    
    @Mock
    private AudioListenerService audioListenerService;
    
    @Mock
    private SilenceDetectionService silenceDetectionService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new AudioStreamingController(
            audioStreamingService,
            audioListenerService,
            silenceDetectionService
        );
    }
    
    @Test
    void testStartSession_Success() {
        // Given
        AudioStreamingController.SessionStartRequest request = new AudioStreamingController.SessionStartRequest();
        request.setSamplesPerSecond(16000);
        request.setBitsPerSample((short) 16);
        request.setChannels((short) 1);
        
        // When
        ResponseEntity<Map<String, Object>> response = controller.startSession(request);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertNotNull(response.getBody().get("sessionId"));
    }
    
    @Test
    void testSendChunk_Success() {
        // Given - first start a session
        AudioStreamingController.SessionStartRequest startRequest = new AudioStreamingController.SessionStartRequest();
        startRequest.setSamplesPerSecond(16000);
        startRequest.setBitsPerSample((short) 16);
        startRequest.setChannels((short) 1);
        ResponseEntity<Map<String, Object>> startResponse = controller.startSession(startRequest);
        String sessionId = (String) startResponse.getBody().get("sessionId");
        
        AudioStreamingController.ChunkRequest chunkRequest = new AudioStreamingController.ChunkRequest();
        chunkRequest.setSessionId(sessionId);
        chunkRequest.setPcmData(new byte[]{1, 2, 3, 4, 5});
        
        when(silenceDetectionService.isSilent(any(), anyLong(), anyShort(), anyShort())).thenReturn(false);
        when(audioStreamingService.getBufferSize()).thenReturn(5);
        
        // When
        ResponseEntity<Map<String, Object>> response = controller.sendChunk(chunkRequest);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(5, response.getBody().get("bufferSize"));
        assertFalse((Boolean) response.getBody().get("isSilent"));
        verify(audioStreamingService).addAudioBytes(any());
    }
    
    @Test
    void testSendChunk_InvalidSession() {
        // Given
        AudioStreamingController.ChunkRequest chunkRequest = new AudioStreamingController.ChunkRequest();
        chunkRequest.setSessionId("invalid-session-id");
        chunkRequest.setPcmData(new byte[]{1, 2, 3, 4, 5});
        
        // When
        ResponseEntity<Map<String, Object>> response = controller.sendChunk(chunkRequest);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("error").toString().contains("Invalid or expired session"));
        verify(audioStreamingService, never()).addAudioBytes(any());
    }
    
    @Test
    void testSendChunk_EmptyData() {
        // Given - first start a session
        AudioStreamingController.SessionStartRequest startRequest = new AudioStreamingController.SessionStartRequest();
        startRequest.setSamplesPerSecond(16000);
        startRequest.setBitsPerSample((short) 16);
        startRequest.setChannels((short) 1);
        ResponseEntity<Map<String, Object>> startResponse = controller.startSession(startRequest);
        String sessionId = (String) startResponse.getBody().get("sessionId");
        
        AudioStreamingController.ChunkRequest chunkRequest = new AudioStreamingController.ChunkRequest();
        chunkRequest.setSessionId(sessionId);
        chunkRequest.setPcmData(new byte[]{});
        
        // When
        ResponseEntity<Map<String, Object>> response = controller.sendChunk(chunkRequest);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("error").toString().contains("PCM data cannot be empty"));
    }
    
    @Test
    void testStopSession_Success() throws Exception {
        // Given - first start a session
        AudioStreamingController.SessionStartRequest startRequest = new AudioStreamingController.SessionStartRequest();
        startRequest.setSamplesPerSecond(16000);
        startRequest.setBitsPerSample((short) 16);
        startRequest.setChannels((short) 1);
        ResponseEntity<Map<String, Object>> startResponse = controller.startSession(startRequest);
        String sessionId = (String) startResponse.getBody().get("sessionId");
        
        AudioStreamingController.SessionStopRequest stopRequest = new AudioStreamingController.SessionStopRequest();
        stopRequest.setSessionId(sessionId);
        
        byte[] testPcmData = new byte[]{1, 2, 3, 4, 5};
        when(audioStreamingService.extractAndClearAudioBytes()).thenReturn(testPcmData);
        
        AudioBean mockAudioBean = new AudioBean(sessionId, new byte[]{1, 2, 3});
        mockAudioBean.setTranscribedText("Test transcription");
        doAnswer(invocation -> {
            AudioBean bean = invocation.getArgument(0);
            bean.setTranscribedText("Test transcription");
            return null;
        }).when(audioListenerService).listenAudio(any());
        
        // When
        ResponseEntity<Map<String, Object>> response = controller.stopSession(stopRequest);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals("Test transcription", response.getBody().get("transcribedText"));
        assertTrue((Boolean) response.getBody().get("hasTranscription"));
        verify(audioStreamingService).extractAndClearAudioBytes();
        verify(audioListenerService).listenAudio(any());
    }
    
    @Test
    void testStopSession_NoAudioData() throws Exception {
        // Given - first start a session
        AudioStreamingController.SessionStartRequest startRequest = new AudioStreamingController.SessionStartRequest();
        startRequest.setSamplesPerSecond(16000);
        startRequest.setBitsPerSample((short) 16);
        startRequest.setChannels((short) 1);
        ResponseEntity<Map<String, Object>> startResponse = controller.startSession(startRequest);
        String sessionId = (String) startResponse.getBody().get("sessionId");
        
        AudioStreamingController.SessionStopRequest stopRequest = new AudioStreamingController.SessionStopRequest();
        stopRequest.setSessionId(sessionId);
        
        when(audioStreamingService.extractAndClearAudioBytes()).thenReturn(new byte[]{});
        
        // When
        ResponseEntity<Map<String, Object>> response = controller.stopSession(stopRequest);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("error").toString().contains("No audio data recorded"));
        verify(audioListenerService, never()).listenAudio(any());
    }
    
    @Test
    void testGetTranscription() throws Exception {
        // Given
        String sessionId = "test-session-id";
        
        // When
        ResponseEntity<Map<String, Object>> response = controller.getTranscription(sessionId);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(sessionId, response.getBody().get("sessionId"));
    }
}
