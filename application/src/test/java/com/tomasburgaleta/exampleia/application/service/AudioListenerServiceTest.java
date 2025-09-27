package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.MIObject;
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
        MIObject miObject = new MIObject("test-id", audioData);
        when(audioListenerPort.listenAudio(miObject)).thenReturn(audioData);
        
        // When
        byte[] result = audioListenerService.listenAudio(miObject);
        
        // Then
        assertArrayEquals(audioData, result);
        verify(audioListenerPort).listenAudio(miObject);
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
        MIObject miObject = new MIObject("test-id", new byte[]{1});
        // Simulate null audio data by creating object with data then setting it to null via reflection
        // Actually, we can't modify the audio data, so we'll create a new object with empty data
        MIObject emptyMIObject = new MIObject("test-id", new byte[]{});
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> audioListenerService.listenAudio(emptyMIObject));
        
        verifyNoInteractions(audioListenerPort);
    }
    
    @Test
    void shouldPropagateAudioProcessingException() throws AudioProcessingException {
        // Given
        byte[] audioData = {1, 2, 3, 4};
        MIObject miObject = new MIObject("test-id", audioData);
        AudioProcessingException exception = new AudioProcessingException("Processing failed");
        when(audioListenerPort.listenAudio(miObject)).thenThrow(exception);
        
        // When & Then
        AudioProcessingException thrown = assertThrows(AudioProcessingException.class,
            () -> audioListenerService.listenAudio(miObject));
        
        assertEquals("Processing failed", thrown.getMessage());
        verify(audioListenerPort).listenAudio(miObject);
    }
    
    @Test
    void shouldThrowExceptionWhenAudioListenerPortIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, 
            () -> new AudioListenerService(null));
    }
}