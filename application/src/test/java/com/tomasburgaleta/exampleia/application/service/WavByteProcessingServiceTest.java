package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import com.tomasburgaleta.exampleia.domain.port.WavByteReaderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WavByteProcessingServiceTest {
    
    @Mock
    private WavByteReaderPort wavByteReaderPort;
    
    private WavByteProcessingService service;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new WavByteProcessingService(wavByteReaderPort);
    }
    
    @Test
    void shouldThrowExceptionWhenPortIsNull() {
        assertThrows(NullPointerException.class, () -> new WavByteProcessingService(null));
    }
    
    @Test
    void shouldThrowExceptionWhenWavBytesIsNull() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> service.processWavBytes(null, "test-id"));
    }
    
    @Test
    void shouldThrowExceptionWhenWavBytesIsEmpty() {
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> service.processWavBytes(new byte[0], "test-id"));
    }
    
    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        // Given
        byte[] wavBytes = {1, 2, 3, 4};
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> service.processWavBytes(wavBytes, null));
    }
    
    @Test
    void shouldThrowExceptionWhenIdIsEmpty() {
        // Given
        byte[] wavBytes = {1, 2, 3, 4};
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> service.processWavBytes(wavBytes, ""));
    }
    
    @Test
    void shouldThrowExceptionWhenIdIsWhitespace() {
        // Given
        byte[] wavBytes = {1, 2, 3, 4};
        
        // When & Then
        assertThrows(IllegalArgumentException.class, 
            () -> service.processWavBytes(wavBytes, "   "));
    }
    
    @Test
    void shouldProcessValidWavBytes() throws AudioFileException {
        // Given
        byte[] wavBytes = createValidWavBytes();
        String id = "test-audio";
        AudioBean expectedBean = new AudioBean(id, new byte[]{1, 2, 3, 4});
        expectedBean.setSamplesPerSecond(44100);
        expectedBean.setBitsPerSample((short) 16);
        expectedBean.setChannels((short) 2);
        
        when(wavByteReaderPort.processWavBytes(wavBytes, id)).thenReturn(expectedBean);
        
        // When
        AudioBean result = service.processWavBytes(wavBytes, id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(44100, result.getSamplesPerSecond());
        assertEquals(16, result.getBitsPerSample());
        assertEquals(2, result.getChannels());
        verify(wavByteReaderPort).processWavBytes(wavBytes, id);
    }
    
    @Test
    void shouldPropagateAudioFileException() throws AudioFileException {
        // Given
        byte[] wavBytes = {1, 2, 3, 4};
        String id = "test-id";
        AudioFileException expectedException = new AudioFileException("Invalid WAV format");
        
        when(wavByteReaderPort.processWavBytes(wavBytes, id)).thenThrow(expectedException);
        
        // When & Then
        AudioFileException actualException = assertThrows(AudioFileException.class,
            () -> service.processWavBytes(wavBytes, id));
        
        assertEquals(expectedException.getMessage(), actualException.getMessage());
        verify(wavByteReaderPort).processWavBytes(wavBytes, id);
    }
    
    private byte[] createValidWavBytes() {
        // This would be a valid WAV byte array in a real test
        // For now, just return some test data
        return new byte[]{
            'R', 'I', 'F', 'F', // RIFF header
            0x24, 0x00, 0x00, 0x00, // File size
            'W', 'A', 'V', 'E' // WAVE format
        };
    }
}