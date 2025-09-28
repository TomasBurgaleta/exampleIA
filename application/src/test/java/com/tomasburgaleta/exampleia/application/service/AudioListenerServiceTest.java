package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AudioListenerServiceTest {
    
    @Mock
    private AudioListenerPort audioListenerPort;
    
    private AudioListenerService audioListenerService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        audioListenerService = new AudioListenerService(audioListenerPort);
    }
    
    @Test
    void shouldProcessAudioSuccessfully() throws AudioProcessingException {
        // Given
        byte[] audioData = {1, 2, 3, 4};
        AudioBean audioBean = new AudioBean("test-id", audioData);
        when(audioListenerPort.listenAudio(audioBean)).thenReturn(audioData);
        
        // When
        byte[] result = audioListenerService.listenAudio(audioBean);
        
        // Then
        assertArrayEquals(audioData, result);
        verify(audioListenerPort).listenAudio(audioBean);
    }
    
    @Test
    void shouldThrowExceptionWhenMIObjectIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, 
            () -> audioListenerService.listenAudio(null));
        
        verifyNoInteractions(audioListenerPort);
    }
    
    @Test
    void shouldThrowExceptionWhenAudioDataIsNull() {
        // Given
        AudioBean audioBean = new AudioBean("test-id", new byte[]{1});
        // Simulate null audio data by creating object with data then setting it to null via reflection
        // Actually, we can't modify the audio data, so we'll create a new object with empty data
        AudioBean emptyAudioBean = new AudioBean("test-id", new byte[]{});
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> audioListenerService.listenAudio(emptyAudioBean));
        
        verifyNoInteractions(audioListenerPort);
    }
    
    @Test
    void shouldPropagateAudioProcessingException() throws AudioProcessingException {
        // Given
        byte[] audioData = {1, 2, 3, 4};
        AudioBean audioBean = new AudioBean("test-id", audioData);
        AudioProcessingException exception = new AudioProcessingException("Processing failed");
        when(audioListenerPort.listenAudio(audioBean)).thenThrow(exception);
        
        // When & Then
        AudioProcessingException thrown = assertThrows(AudioProcessingException.class,
            () -> audioListenerService.listenAudio(audioBean));
        
        assertEquals("Processing failed", thrown.getMessage());
        verify(audioListenerPort).listenAudio(audioBean);
    }
    
    @Test
    void shouldThrowExceptionWhenAudioListenerPortIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, 
            () -> new AudioListenerService(null));
    }
}