package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import com.tomasburgaleta.exampleia.domain.port.AudioFileReaderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AudioFileReaderService
 */
@ExtendWith(MockitoExtension.class)
class AudioFileReaderServiceTest {
    
    @Mock
    private AudioFileReaderPort audioFileReaderPort;
    
    private AudioFileReaderService audioFileReaderService;
    
    @BeforeEach
    void setUp() {
        audioFileReaderService = new AudioFileReaderService(audioFileReaderPort);
    }
    
    @Test
    void constructor_shouldThrowException_whenPortIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new AudioFileReaderService(null));
    }
    
    @Test
    void readWavFile_shouldCallPortAndReturnBytes_whenValidPathProvided() throws AudioFileException {
        // Arrange
        String filePath = "/path/to/audio.wav";
        byte[] expectedBytes = new byte[]{1, 2, 3, 4};
        when(audioFileReaderPort.readWavFile(filePath)).thenReturn(expectedBytes);
        
        // Act
        byte[] result = audioFileReaderService.readWavFile(filePath);
        
        // Assert
        assertArrayEquals(expectedBytes, result);
        verify(audioFileReaderPort).readWavFile(filePath);
    }
    
    @Test
    void readWavFile_shouldThrowIllegalArgumentException_whenPathIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> audioFileReaderService.readWavFile(null));
        verifyNoInteractions(audioFileReaderPort);
    }
    
    @Test
    void readWavFile_shouldThrowIllegalArgumentException_whenPathIsEmpty() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> audioFileReaderService.readWavFile(""));
        verifyNoInteractions(audioFileReaderPort);
    }
    
    @Test
    void readWavFile_shouldThrowIllegalArgumentException_whenPathIsWhitespace() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> audioFileReaderService.readWavFile("   "));
        verifyNoInteractions(audioFileReaderPort);
    }
    
    @Test
    void readWavFile_shouldPropagateAudioFileException_whenPortThrowsException() throws AudioFileException {
        // Arrange
        String filePath = "/path/to/invalid.wav";
        AudioFileException expectedException = new AudioFileException("Invalid file format");
        when(audioFileReaderPort.readWavFile(filePath)).thenThrow(expectedException);
        
        // Act & Assert
        AudioFileException thrownException = assertThrows(AudioFileException.class, 
            () -> audioFileReaderService.readWavFile(filePath));
        
        assertEquals(expectedException, thrownException);
        verify(audioFileReaderPort).readWavFile(filePath);
    }
}