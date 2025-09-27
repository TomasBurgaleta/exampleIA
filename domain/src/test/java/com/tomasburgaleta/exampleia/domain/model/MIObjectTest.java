package com.tomasburgaleta.exampleia.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MIObjectTest {
    
    @Test
    void shouldCreateMIObjectWithValidData() {
        // Given
        String id = "test-id";
        byte[] audioData = {1, 2, 3, 4};
        
        // When
        MIObject miObject = new MIObject(id, audioData);
        
        // Then
        assertEquals(id, miObject.getId());
        assertArrayEquals(audioData, miObject.getAudioData());
        assertNull(miObject.getTranscribedText());
        assertFalse(miObject.hasTranscribedText());
    }
    
    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        // Given
        byte[] audioData = {1, 2, 3, 4};
        
        // When & Then
        assertThrows(NullPointerException.class, () -> new MIObject(null, audioData));
    }
    
    @Test
    void shouldThrowExceptionWhenAudioDataIsNull() {
        // Given
        String id = "test-id";
        
        // When & Then
        assertThrows(NullPointerException.class, () -> new MIObject(id, null));
    }
    
    @Test
    void shouldSetAndGetTranscribedText() {
        // Given
        MIObject miObject = new MIObject("test-id", new byte[]{1, 2, 3});
        String transcribedText = "Hello world";
        
        // When
        miObject.setTranscribedText(transcribedText);
        
        // Then
        assertEquals(transcribedText, miObject.getTranscribedText());
        assertTrue(miObject.hasTranscribedText());
    }
    
    @Test
    void shouldReturnFalseForHasTranscribedTextWhenEmpty() {
        // Given
        MIObject miObject = new MIObject("test-id", new byte[]{1, 2, 3});
        
        // When
        miObject.setTranscribedText("");
        
        // Then
        assertFalse(miObject.hasTranscribedText());
    }
    
    @Test
    void shouldReturnFalseForHasTranscribedTextWhenWhitespace() {
        // Given
        MIObject miObject = new MIObject("test-id", new byte[]{1, 2, 3});
        
        // When
        miObject.setTranscribedText("   ");
        
        // Then
        assertFalse(miObject.hasTranscribedText());
    }
}