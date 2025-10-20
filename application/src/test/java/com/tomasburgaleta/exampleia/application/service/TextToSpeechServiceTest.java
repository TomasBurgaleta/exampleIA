package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.domain.port.TextToSpeechPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TextToSpeechServiceTest {
    
    @Mock
    private TextToSpeechPort textToSpeechPort;
    
    private TextToSpeechService textToSpeechService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        textToSpeechService = new TextToSpeechService(textToSpeechPort);
    }
    
    @Test
    void shouldConvertTextToSpeechSuccessfully() throws AudioProcessingException {
        // Given
        String text = "Hello, this is a test";
        byte[] expectedAudio = {1, 2, 3, 4, 5};
        when(textToSpeechPort.isAvailable()).thenReturn(true);
        when(textToSpeechPort.synthesizeSpeech(text)).thenReturn(expectedAudio);
        
        // When
        byte[] result = textToSpeechService.convertTextToSpeech(text);
        
        // Then
        assertArrayEquals(expectedAudio, result);
        verify(textToSpeechPort).isAvailable();
        verify(textToSpeechPort).synthesizeSpeech(text);
    }
    
    @Test
    void shouldThrowExceptionWhenTextIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, 
            () -> textToSpeechService.convertTextToSpeech(null));
        
        verifyNoInteractions(textToSpeechPort);
    }
    
    @Test
    void shouldThrowExceptionWhenTextIsEmpty() {
        // When & Then
        AudioProcessingException exception = assertThrows(AudioProcessingException.class,
            () -> textToSpeechService.convertTextToSpeech(""));
        
        assertEquals("Text cannot be empty", exception.getMessage());
        verifyNoInteractions(textToSpeechPort);
    }
    
    @Test
    void shouldThrowExceptionWhenTextIsBlank() {
        // When & Then
        AudioProcessingException exception = assertThrows(AudioProcessingException.class,
            () -> textToSpeechService.convertTextToSpeech("   "));
        
        assertEquals("Text cannot be empty", exception.getMessage());
        verifyNoInteractions(textToSpeechPort);
    }
    
    @Test
    void shouldThrowExceptionWhenServiceNotAvailable() throws AudioProcessingException {
        // Given
        String text = "Hello, world";
        when(textToSpeechPort.isAvailable()).thenReturn(false);
        
        // When & Then
        AudioProcessingException exception = assertThrows(AudioProcessingException.class,
            () -> textToSpeechService.convertTextToSpeech(text));
        
        assertEquals("Text-to-Speech service is not available. Please check configuration.", exception.getMessage());
        verify(textToSpeechPort).isAvailable();
        verify(textToSpeechPort, never()).synthesizeSpeech(anyString());
    }
    
    @Test
    void shouldPropagateAudioProcessingException() throws AudioProcessingException {
        // Given
        String text = "Test text";
        AudioProcessingException exception = new AudioProcessingException("TTS service failed");
        when(textToSpeechPort.isAvailable()).thenReturn(true);
        when(textToSpeechPort.synthesizeSpeech(text)).thenThrow(exception);
        
        // When & Then
        AudioProcessingException thrown = assertThrows(AudioProcessingException.class,
            () -> textToSpeechService.convertTextToSpeech(text));
        
        assertEquals("TTS service failed", thrown.getMessage());
        verify(textToSpeechPort).isAvailable();
        verify(textToSpeechPort).synthesizeSpeech(text);
    }
    
    @Test
    void shouldReturnTrueWhenServiceIsAvailable() {
        // Given
        when(textToSpeechPort.isAvailable()).thenReturn(true);
        
        // When
        boolean result = textToSpeechService.isServiceAvailable();
        
        // Then
        assertTrue(result);
        verify(textToSpeechPort).isAvailable();
    }
    
    @Test
    void shouldReturnFalseWhenServiceIsNotAvailable() {
        // Given
        when(textToSpeechPort.isAvailable()).thenReturn(false);
        
        // When
        boolean result = textToSpeechService.isServiceAvailable();
        
        // Then
        assertFalse(result);
        verify(textToSpeechPort).isAvailable();
    }
    
    @Test
    void shouldThrowExceptionWhenTextToSpeechPortIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, 
            () -> new TextToSpeechService(null));
    }
}
