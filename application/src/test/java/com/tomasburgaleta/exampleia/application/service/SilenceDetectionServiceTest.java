package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.port.SilenceDetectorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SilenceDetectionServiceTest {
    
    @Mock
    private SilenceDetectorPort silenceDetectorPort;
    
    private SilenceDetectionService silenceDetectionService;
    
    @BeforeEach
    void setUp() {
        silenceDetectionService = new SilenceDetectionService(silenceDetectorPort);
    }
    
    @Test
    void testConstructorWithNullPort() {
        assertThrows(NullPointerException.class, () -> 
            new SilenceDetectionService(null));
    }
    
    @Test
    void testIsSilent_NullData() {
        assertThrows(NullPointerException.class, () -> 
            silenceDetectionService.isSilent(null, 44100, (short) 16, (short) 1));
    }
    
    @Test
    void testIsSilent_EmptyData() {
        byte[] emptyData = new byte[0];
        boolean result = silenceDetectionService.isSilent(emptyData, 44100, (short) 16, (short) 1);
        assertTrue(result, "Empty data should be considered silent");
        
        verify(silenceDetectorPort, never()).detectSilence(any(), anyLong(), anyShort(), anyShort());
    }
    
    @Test
    void testIsSilent_SilentAudio() {
        byte[] pcmData = new byte[]{0, 0, 0, 0, 0, 0};
        when(silenceDetectorPort.detectSilence(pcmData, 44100, (short) 16, (short) 1))
            .thenReturn(true);
        
        boolean result = silenceDetectionService.isSilent(pcmData, 44100, (short) 16, (short) 1);
        
        assertTrue(result);
        verify(silenceDetectorPort, times(1)).detectSilence(pcmData, 44100, (short) 16, (short) 1);
    }
    
    @Test
    void testIsSilent_AudibleAudio() {
        byte[] pcmData = new byte[]{1, 2, 3, 4, 5, 6};
        when(silenceDetectorPort.detectSilence(pcmData, 44100, (short) 16, (short) 1))
            .thenReturn(false);
        
        boolean result = silenceDetectionService.isSilent(pcmData, 44100, (short) 16, (short) 1);
        
        assertFalse(result);
        verify(silenceDetectorPort, times(1)).detectSilence(pcmData, 44100, (short) 16, (short) 1);
    }
    
    @Test
    void testIsSilent_DifferentSampleRates() {
        byte[] pcmData = new byte[]{1, 2, 3, 4};
        
        // Test with 8kHz
        when(silenceDetectorPort.detectSilence(pcmData, 8000, (short) 16, (short) 1))
            .thenReturn(false);
        assertFalse(silenceDetectionService.isSilent(pcmData, 8000, (short) 16, (short) 1));
        
        // Test with 16kHz
        when(silenceDetectorPort.detectSilence(pcmData, 16000, (short) 16, (short) 1))
            .thenReturn(false);
        assertFalse(silenceDetectionService.isSilent(pcmData, 16000, (short) 16, (short) 1));
        
        // Test with 44.1kHz
        when(silenceDetectorPort.detectSilence(pcmData, 44100, (short) 16, (short) 1))
            .thenReturn(false);
        assertFalse(silenceDetectionService.isSilent(pcmData, 44100, (short) 16, (short) 1));
        
        verify(silenceDetectorPort, times(3)).detectSilence(eq(pcmData), anyLong(), anyShort(), anyShort());
    }
    
    @Test
    void testIsSilent_DifferentBitDepths() {
        byte[] pcmData = new byte[]{1, 2, 3, 4};
        
        // Test with 8-bit
        when(silenceDetectorPort.detectSilence(pcmData, 44100, (short) 8, (short) 1))
            .thenReturn(false);
        assertFalse(silenceDetectionService.isSilent(pcmData, 44100, (short) 8, (short) 1));
        
        // Test with 16-bit
        when(silenceDetectorPort.detectSilence(pcmData, 44100, (short) 16, (short) 1))
            .thenReturn(false);
        assertFalse(silenceDetectionService.isSilent(pcmData, 44100, (short) 16, (short) 1));
        
        // Test with 24-bit
        when(silenceDetectorPort.detectSilence(pcmData, 44100, (short) 24, (short) 1))
            .thenReturn(false);
        assertFalse(silenceDetectionService.isSilent(pcmData, 44100, (short) 24, (short) 1));
        
        verify(silenceDetectorPort, times(3)).detectSilence(eq(pcmData), anyLong(), anyShort(), anyShort());
    }
    
    @Test
    void testIsSilent_DifferentChannels() {
        byte[] pcmData = new byte[]{1, 2, 3, 4};
        
        // Test with mono
        when(silenceDetectorPort.detectSilence(pcmData, 44100, (short) 16, (short) 1))
            .thenReturn(false);
        assertFalse(silenceDetectionService.isSilent(pcmData, 44100, (short) 16, (short) 1));
        
        // Test with stereo
        when(silenceDetectorPort.detectSilence(pcmData, 44100, (short) 16, (short) 2))
            .thenReturn(false);
        assertFalse(silenceDetectionService.isSilent(pcmData, 44100, (short) 16, (short) 2));
        
        verify(silenceDetectorPort, times(2)).detectSilence(eq(pcmData), anyLong(), anyShort(), anyShort());
    }
}
