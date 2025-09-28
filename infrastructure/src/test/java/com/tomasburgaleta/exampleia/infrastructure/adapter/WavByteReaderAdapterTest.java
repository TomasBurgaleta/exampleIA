package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WavByteReaderAdapterTest {
    
    private WavByteReaderAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new WavByteReaderAdapter();
    }
    
    @Test
    void shouldThrowExceptionWhenWavBytesIsNull() {
        assertThrows(IllegalArgumentException.class, 
            () -> adapter.processWavBytes(null, "test-id"));
    }
    
    @Test
    void shouldThrowExceptionWhenWavBytesIsEmpty() {
        assertThrows(IllegalArgumentException.class, 
            () -> adapter.processWavBytes(new byte[0], "test-id"));
    }
    
    @Test
    void shouldThrowExceptionWhenIdIsNull() {
        byte[] wavBytes = createValidWavBytes();
        assertThrows(IllegalArgumentException.class, 
            () -> adapter.processWavBytes(wavBytes, null));
    }
    
    @Test
    void shouldThrowExceptionWhenIdIsEmpty() {
        byte[] wavBytes = createValidWavBytes();
        assertThrows(IllegalArgumentException.class, 
            () -> adapter.processWavBytes(wavBytes, ""));
    }
    
    @Test
    void shouldThrowExceptionWhenDataIsTooSmall() {
        byte[] invalidWav = {1, 2, 3}; // Too small for WAV header
        
        AudioFileException exception = assertThrows(AudioFileException.class,
            () -> adapter.processWavBytes(invalidWav, "test-id"));
        
        assertTrue(exception.getMessage().contains("too small"));
    }
    
    @Test
    void shouldThrowExceptionWhenMissingRiffHeader() {
        byte[] invalidWav = {
            'X', 'I', 'F', 'F', // Wrong RIFF header
            0x24, 0x00, 0x00, 0x00,
            'W', 'A', 'V', 'E'
        };
        
        AudioFileException exception = assertThrows(AudioFileException.class,
            () -> adapter.processWavBytes(invalidWav, "test-id"));
        
        assertTrue(exception.getMessage().contains("RIFF header"));
    }
    
    @Test
    void shouldThrowExceptionWhenMissingWaveFormat() {
        byte[] invalidWav = {
            'R', 'I', 'F', 'F',
            0x24, 0x00, 0x00, 0x00,
            'W', 'A', 'V', 'X' // Wrong WAVE format
        };
        
        AudioFileException exception = assertThrows(AudioFileException.class,
            () -> adapter.processWavBytes(invalidWav, "test-id"));
        
        assertTrue(exception.getMessage().contains("WAVE format"));
    }
    
    @Test
    void shouldProcessValidWavBytesSuccessfully() throws AudioFileException {
        // Given
        byte[] validWav = createValidWavBytes();
        String id = "test-audio";
        
        // When
        AudioBean result = adapter.processWavBytes(validWav, id);
        
        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(44100L, result.getSamplesPerSecond());
        assertEquals(16, result.getBitsPerSample());
        assertEquals(2, result.getChannels());
        assertNotNull(result.getAudioData());
        assertTrue(result.getAudioData().length > 0);
    }
    
    @Test
    void shouldExtractCorrectPcmData() throws AudioFileException {
        // Given
        byte[] testPcmData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06};
        byte[] validWav = createValidWavBytesWithData(testPcmData);
        String id = "test-audio";
        
        // When
        AudioBean result = adapter.processWavBytes(validWav, id);
        
        // Then
        assertArrayEquals(testPcmData, result.getAudioData());
    }
    
    @Test
    void shouldHandleMonoAudio() throws AudioFileException {
        // Given
        byte[] monoWav = createMonoWavBytes();
        String id = "mono-audio";
        
        // When
        AudioBean result = adapter.processWavBytes(monoWav, id);
        
        // Then
        assertEquals(1, result.getChannels());
        assertEquals(22050L, result.getSamplesPerSecond());
        assertEquals(8, result.getBitsPerSample());
    }
    
    @Test
    void shouldThrowExceptionWhenFmtChunkNotFound() {
        // Given - WAV without fmt chunk
        byte[] wavWithoutFmt = {
            'R', 'I', 'F', 'F',
            0x20, 0x00, 0x00, 0x00,
            'W', 'A', 'V', 'E',
            'd', 'a', 't', 'a', // Skip fmt, go directly to data
            0x04, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04
        };
        
        // When & Then
        AudioFileException exception = assertThrows(AudioFileException.class,
            () -> adapter.processWavBytes(wavWithoutFmt, "test-id"));
        
        assertTrue(exception.getMessage().contains("fmt chunk not found"));
    }
    
    @Test
    void shouldThrowExceptionWhenDataChunkNotFound() {
        // Given - WAV without data chunk
        byte[] wavWithoutData = {
            'R', 'I', 'F', 'F',
            0x24, 0x00, 0x00, 0x00,
            'W', 'A', 'V', 'E',
            'f', 'm', 't', ' ',
            0x10, 0x00, 0x00, 0x00, // fmt chunk size
            0x01, 0x00, // PCM format
            0x02, 0x00, // 2 channels
            0x44, (byte) 0xAC, 0x00, 0x00, // 44100 Hz
            0x10, (byte) 0xB1, 0x02, 0x00, // byte rate
            0x04, 0x00, // block align
            0x10, 0x00  // 16 bits per sample
            // Missing data chunk
        };
        
        // When & Then
        AudioFileException exception = assertThrows(AudioFileException.class,
            () -> adapter.processWavBytes(wavWithoutData, "test-id"));
        
        assertTrue(exception.getMessage().contains("data chunk not found"));
    }
    
    /**
     * Creates a valid WAV byte array for testing
     */
    private byte[] createValidWavBytes() {
        return new byte[]{
            // RIFF header
            'R', 'I', 'F', 'F',
            0x2C, 0x00, 0x00, 0x00, // File size (44 bytes)
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
            0x08, 0x00, 0x00, 0x00, // data chunk size (8 bytes)
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 // PCM data
        };
    }
    
    /**
     * Creates a valid WAV byte array with custom PCM data for testing
     */
    private byte[] createValidWavBytesWithData(byte[] pcmData) {
        byte[] header = {
            // RIFF header
            'R', 'I', 'F', 'F',
            (byte) (36 + pcmData.length), 0x00, 0x00, 0x00, // File size
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
            (byte) pcmData.length, 0x00, 0x00, 0x00 // data chunk size
        };
        
        // Combine header and PCM data
        byte[] result = new byte[header.length + pcmData.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(pcmData, 0, result, header.length, pcmData.length);
        
        return result;
    }
    
    /**
     * Creates a mono WAV byte array for testing
     */
    private byte[] createMonoWavBytes() {
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