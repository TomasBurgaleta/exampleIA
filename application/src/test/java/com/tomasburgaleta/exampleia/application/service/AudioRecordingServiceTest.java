package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AiServicePort;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioRecordingServiceTest {
    
    @Mock
    private AudioRecordingPort audioRecordingPort;
    
    @Mock
    private AudioListenerPort audioListenerPort;
    
    @Mock
    private SilenceDetectionService silenceDetectionService;
    
    @Mock
    private AiServicePort aiServicePort;
    
    private AudioRecordingService audioRecordingService;
    
    @BeforeEach
    void setUp() {
        audioRecordingService = new AudioRecordingService(audioRecordingPort, audioListenerPort, silenceDetectionService, aiServicePort);
    }
    
    @Test
    void testConstructorWithNullPort() {
        assertThrows(NullPointerException.class, () -> new AudioRecordingService(null, audioListenerPort, silenceDetectionService, aiServicePort));
    }
    
    @Test
    void testStartRecording_Success() {
        // Arrange
        byte[] pcmData = new byte[]{1, 2, 3, 4, 5};
        long samplesPerSecond = 44100;
        short bitsPerSample = 16;
        short channels = 2;
        
        when(audioRecordingPort.storeRecording(any(AudioBean.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        AudioBean result = audioRecordingService.startRecording(pcmData, samplesPerSecond, bitsPerSample, channels);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(samplesPerSecond, result.getSamplesPerSecond());
        assertEquals(bitsPerSample, result.getBitsPerSample());
        assertEquals(channels, result.getChannels());
        assertArrayEquals(pcmData, result.getAudioData());
        
        verify(audioRecordingPort, times(1)).storeRecording(any(AudioBean.class));
    }
    
    @Test
    void testStartRecording_NullPcmData() {
        assertThrows(NullPointerException.class, () -> 
            audioRecordingService.startRecording(null, 44100, (short) 16, (short) 2));
    }
    
    @Test
    void testStartRecording_EmptyPcmData() {
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.startRecording(new byte[0], 44100, (short) 16, (short) 2));
    }
    
    @Test
    void testStartRecording_InvalidSamplesPerSecond() {
        byte[] pcmData = new byte[]{1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.startRecording(pcmData, 0, (short) 16, (short) 2));
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.startRecording(pcmData, -1, (short) 16, (short) 2));
    }
    
    @Test
    void testStartRecording_InvalidBitsPerSample() {
        byte[] pcmData = new byte[]{1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.startRecording(pcmData, 44100, (short) 0, (short) 2));
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.startRecording(pcmData, 44100, (short) -1, (short) 2));
    }
    
    @Test
    void testStartRecording_InvalidChannels() {
        byte[] pcmData = new byte[]{1, 2, 3};
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.startRecording(pcmData, 44100, (short) 16, (short) 0));
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.startRecording(pcmData, 44100, (short) 16, (short) -1));
    }
    
    @Test
    void testGetRecording_Success() {
        // Arrange
        String id = "test-id";
        AudioBean expectedBean = new AudioBean(id, new byte[]{1, 2, 3});
        when(audioRecordingPort.getRecording(id)).thenReturn(expectedBean);
        
        // Act
        AudioBean result = audioRecordingService.getRecording(id);
        
        // Assert
        assertNotNull(result);
        assertEquals(expectedBean, result);
        verify(audioRecordingPort, times(1)).getRecording(id);
    }
    
    @Test
    void testGetRecording_NotFound() {
        // Arrange
        String id = "non-existent-id";
        when(audioRecordingPort.getRecording(id)).thenReturn(null);
        
        // Act
        AudioBean result = audioRecordingService.getRecording(id);
        
        // Assert
        assertNull(result);
        verify(audioRecordingPort, times(1)).getRecording(id);
    }
    
    @Test
    void testGetRecording_NullId() {
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.getRecording(null));
    }
    
    @Test
    void testGetRecording_EmptyId() {
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.getRecording(""));
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.getRecording("   "));
    }
    
    @Test
    void testStopRecording_Success() {
        // Arrange
        String id = "test-id";
        when(audioRecordingPort.clearRecording(id)).thenReturn(true);
        
        // Act
        boolean result = audioRecordingService.stopRecording(id);
        
        // Assert
        assertTrue(result);
        verify(audioRecordingPort, times(1)).clearRecording(id);
    }
    
    @Test
    void testStopRecording_NotFound() {
        // Arrange
        String id = "non-existent-id";
        when(audioRecordingPort.clearRecording(id)).thenReturn(false);
        
        // Act
        boolean result = audioRecordingService.stopRecording(id);
        
        // Assert
        assertFalse(result);
        verify(audioRecordingPort, times(1)).clearRecording(id);
    }
    
    @Test
    void testStopRecording_NullId() {
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.stopRecording(null));
    }
    
    @Test
    void testStopRecording_EmptyId() {
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.stopRecording(""));
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.stopRecording("   "));
    }
}
