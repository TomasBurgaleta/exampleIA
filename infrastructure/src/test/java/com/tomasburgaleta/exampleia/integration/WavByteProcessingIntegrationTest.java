package com.tomasburgaleta.exampleia.integration;

import com.tomasburgaleta.exampleia.application.service.WavByteProcessingService;
import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import com.tomasburgaleta.exampleia.infrastructure.adapter.WavByteReaderAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for WAV byte processing functionality
 */
class WavByteProcessingIntegrationTest {
    
    private WavByteProcessingService service;
    
    @BeforeEach
    void setUp() {
        WavByteReaderAdapter adapter = new WavByteReaderAdapter();
        service = new WavByteProcessingService(adapter);
    }
    
    @Test
    void shouldProcessValidWavBytesSuccessfully() throws AudioFileException {
        // Given
        byte[] validWav = createValidStereoWav();
        String id = "integration-test-audio";
        
        // When
        AudioBean result = service.processWavBytes(validWav, id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(44100L, result.getSamplesPerSecond());
        assertEquals(16, result.getBitsPerSample());
        assertEquals(2, result.getChannels());
        assertNotNull(result.getAudioData());
        assertEquals(8, result.getAudioData().length); // Our test PCM data is 8 bytes
        
        // Verify PCM data is extracted correctly
        byte[] expectedPcmData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        assertArrayEquals(expectedPcmData, result.getAudioData());
    }
    
    @Test
    void shouldProcessMonoWavSuccessfully() throws AudioFileException {
        // Given
        byte[] monoWav = createValidMonoWav();
        String id = "mono-test";
        
        // When
        AudioBean result = service.processWavBytes(monoWav, id);
        
        // Then
        assertEquals(1, result.getChannels());
        assertEquals(22050L, result.getSamplesPerSecond());
        assertEquals(8, result.getBitsPerSample());
        assertEquals(4, result.getAudioData().length);
    }
    
    @Test
    void shouldRejectInvalidWavData() {
        // Given - Invalid WAV data
        byte[] invalidData = {1, 2, 3, 4, 5};
        String id = "invalid-test";
        
        // When & Then
        AudioFileException exception = assertThrows(AudioFileException.class,
            () -> service.processWavBytes(invalidData, id));
        
        assertTrue(exception.getMessage().contains("too small"));
    }
    
    @Test
    void shouldRejectNonPcmFormat() {
        // Given - WAV with non-PCM format (μ-law = 7)
        byte[] nonPcmWav = {
            'R', 'I', 'F', 'F',
            0x2C, 0x00, 0x00, 0x00,
            'W', 'A', 'V', 'E',
            'f', 'm', 't', ' ',
            0x10, 0x00, 0x00, 0x00,
            0x07, 0x00, // μ-law format (not PCM)
            0x01, 0x00, // 1 channel
            0x44, (byte) 0xAC, 0x00, 0x00, // 44100 Hz
            0x44, (byte) 0xAC, 0x00, 0x00, // byte rate
            0x01, 0x00, // block align
            0x08, 0x00, // 8 bits per sample
            'd', 'a', 't', 'a',
            0x04, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04
        };
        String id = "non-pcm-test";
        
        // When & Then
        AudioFileException exception = assertThrows(AudioFileException.class,
            () -> service.processWavBytes(nonPcmWav, id));
        
        assertTrue(exception.getMessage().contains("Only PCM format is supported"));
    }
    
    /**
     * Creates a valid stereo WAV byte array (44.1kHz, 16-bit, 2 channels)
     */
    private byte[] createValidStereoWav() {
        byte[] pcmData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        
        return new byte[]{
            // RIFF header
            'R', 'I', 'F', 'F',
            (byte) (36 + pcmData.length), 0x00, 0x00, 0x00,
            'W', 'A', 'V', 'E',
            
            // fmt chunk
            'f', 'm', 't', ' ',
            0x10, 0x00, 0x00, 0x00, // fmt chunk size (16)
            0x01, 0x00, // PCM format
            0x02, 0x00, // 2 channels (stereo)
            0x44, (byte) 0xAC, 0x00, 0x00, // 44100 Hz sample rate
            0x10, (byte) 0xB1, 0x02, 0x00, // byte rate
            0x04, 0x00, // block align
            0x10, 0x00, // 16 bits per sample
            
            // data chunk  
            'd', 'a', 't', 'a',
            (byte) pcmData.length, 0x00, 0x00, 0x00, // data chunk size
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 // PCM data
        };
    }
    
    /**
     * Creates a valid mono WAV byte array (22.05kHz, 8-bit, 1 channel)
     */
    private byte[] createValidMonoWav() {
        return new byte[]{
            // RIFF header
            'R', 'I', 'F', 'F',
            0x28, 0x00, 0x00, 0x00, // File size
            'W', 'A', 'V', 'E',
            
            // fmt chunk
            'f', 'm', 't', ' ',
            0x10, 0x00, 0x00, 0x00, // fmt chunk size (16)
            0x01, 0x00, // PCM format
            0x01, 0x00, // 1 channel (mono)
            0x22, 0x56, 0x00, 0x00, // 22050 Hz sample rate
            0x22, 0x56, 0x00, 0x00, // byte rate
            0x01, 0x00, // block align
            0x08, 0x00, // 8 bits per sample
            
            // data chunk
            'd', 'a', 't', 'a',
            0x04, 0x00, 0x00, 0x00, // data chunk size (4 bytes)
            (byte) 0x80, (byte) 0x90, (byte) 0xA0, (byte) 0xB0 // PCM data
        };
    }
}