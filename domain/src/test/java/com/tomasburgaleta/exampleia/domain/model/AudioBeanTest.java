package com.tomasburgaleta.exampleia.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AudioBeanTest {
    
    @Test
    void shouldCreateMIObjectWithValidData() {
        // Given
        String id = "test-id";
        byte[] audioData = {1, 2, 3, 4};
        
        // When
        AudioBean audioBean = new AudioBean(id, audioData);
        
        // Then
        assertEquals(id, audioBean.getId());
        assertArrayEquals(audioData, audioBean.getAudioData());
        assertNull(audioBean.getTranscribedText());
        assertFalse(audioBean.hasTranscribedText());
    }
    
    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        // Given
        byte[] audioData = {1, 2, 3, 4};
        
        // When & Then
        assertThrows(NullPointerException.class, () -> new AudioBean(null, audioData));
    }
    
    @Test
    void shouldThrowExceptionWhenAudioDataIsNull() {
        // Given
        String id = "test-id";
        
        // When & Then
        assertThrows(NullPointerException.class, () -> new AudioBean(id, null));
    }
    
    @Test
    void shouldSetAndGetTranscribedText() {
        // Given
        AudioBean audioBean = new AudioBean("test-id", new byte[]{1, 2, 3});
        String transcribedText = "Hello world";
        
        // When
        audioBean.setTranscribedText(transcribedText);
        
        // Then
        assertEquals(transcribedText, audioBean.getTranscribedText());
        assertTrue(audioBean.hasTranscribedText());
    }
    
    @Test
    void shouldReturnFalseForHasTranscribedTextWhenEmpty() {
        // Given
        AudioBean audioBean = new AudioBean("test-id", new byte[]{1, 2, 3});
        
        // When
        audioBean.setTranscribedText("");
        
        // Then
        assertFalse(audioBean.hasTranscribedText());
    }
    
    @Test
    void shouldReturnFalseForHasTranscribedTextWhenWhitespace() {
        // Given
        AudioBean audioBean = new AudioBean("test-id", new byte[]{1, 2, 3});
        
        // When
        audioBean.setTranscribedText("   ");
        
        // Then
        assertFalse(audioBean.hasTranscribedText());
    }
}