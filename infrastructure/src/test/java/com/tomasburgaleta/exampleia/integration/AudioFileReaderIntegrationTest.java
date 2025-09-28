package com.tomasburgaleta.exampleia.integration;

import com.tomasburgaleta.exampleia.application.service.AudioFileReaderService;
import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import com.tomasburgaleta.exampleia.infrastructure.adapter.FileSystemAudioFileReaderAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for AudioFileReader functionality
 */
class AudioFileReaderIntegrationTest {
    
    private AudioFileReaderService audioFileReaderService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Create the complete service using the actual adapter
        FileSystemAudioFileReaderAdapter adapter = new FileSystemAudioFileReaderAdapter();
        audioFileReaderService = new AudioFileReaderService(adapter);
    }
    
    @Test
    void shouldReadValidWavFileSuccessfully() throws IOException, AudioFileException {
        // Arrange
        Path wavFile = createValidWavFile(tempDir.resolve("integration-test.wav"));
        
        // Act
        byte[] result = audioFileReaderService.readWavFile(wavFile.toString());
        
        // Assert
        assertNotNull(result);
        assertTrue(result.length >= 8); // Should have at least the PCM data (8 bytes)
        
        // The result should be the PCM data without the WAV header
        // With our test data, it should be exactly 8 bytes of PCM data
        assertEquals(8, result.length);
    }
    
    @Test
    void shouldThrowExceptionForInvalidFormat() throws IOException {
        // Arrange
        Path invalidFile = tempDir.resolve("invalid.txt");
        Files.write(invalidFile, "This is not a WAV file".getBytes());
        
        // Act & Assert
        AudioFileException exception = assertThrows(AudioFileException.class, 
            () -> audioFileReaderService.readWavFile(invalidFile.toString()));
        
        assertTrue(exception.getMessage().contains("missing RIFF header"));
    }
    
    @Test
    void shouldThrowExceptionForNonExistentFile() {
        // Arrange
        String nonExistentPath = tempDir.resolve("does-not-exist.wav").toString();
        
        // Act & Assert
        AudioFileException exception = assertThrows(AudioFileException.class, 
            () -> audioFileReaderService.readWavFile(nonExistentPath));
        
        assertTrue(exception.getMessage().contains("File not found"));
    }
    
    /**
     * Creates a valid WAV file for testing
     */
    private Path createValidWavFile(Path filePath) throws IOException {
        byte[] wavHeader = createValidWavHeader();
        Files.write(filePath, wavHeader);
        return filePath;
    }
    
    /**
     * Creates a minimal valid WAV header with actual PCM data for testing
     */
    private byte[] createValidWavHeader() {
        byte[] pcmData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08}; // 8 bytes of PCM data
        byte[] wav = new byte[44 + pcmData.length]; // Header + PCM data
        
        // RIFF header
        wav[0] = 'R';
        wav[1] = 'I';
        wav[2] = 'F';
        wav[3] = 'F';
        
        // File size (36 bytes after this point + PCM data)
        int fileSize = 36 + pcmData.length;
        wav[4] = (byte) (fileSize & 0xFF);
        wav[5] = (byte) ((fileSize >> 8) & 0xFF);
        wav[6] = (byte) ((fileSize >> 16) & 0xFF);
        wav[7] = (byte) ((fileSize >> 24) & 0xFF);
        
        // WAVE format
        wav[8] = 'W';
        wav[9] = 'A';
        wav[10] = 'V';
        wav[11] = 'E';
        
        // fmt sub-chunk
        wav[12] = 'f';
        wav[13] = 'm';
        wav[14] = 't';
        wav[15] = ' ';
        
        // Sub-chunk size (16 bytes)
        wav[16] = 0x10;
        wav[17] = 0x00;
        wav[18] = 0x00;
        wav[19] = 0x00;
        
        // Audio format (PCM = 1)
        wav[20] = 0x01;
        wav[21] = 0x00;
        
        // Number of channels (1)
        wav[22] = 0x01;
        wav[23] = 0x00;
        
        // Sample rate (44100 Hz)
        wav[24] = 0x44;
        wav[25] = (byte) 0xAC;
        wav[26] = 0x00;
        wav[27] = 0x00;
        
        // Byte rate (44100 * 1 * 2 = 88200)
        wav[28] = (byte) 0x88;
        wav[29] = 0x58;
        wav[30] = 0x01;
        wav[31] = 0x00;
        
        // Block align (1 * 2 = 2)
        wav[32] = 0x02;
        wav[33] = 0x00;
        
        // Bits per sample (16)
        wav[34] = 0x10;
        wav[35] = 0x00;
        
        // data sub-chunk
        wav[36] = 'd';
        wav[37] = 'a';
        wav[38] = 't';
        wav[39] = 'a';
        
        // Data size (PCM data length)
        wav[40] = (byte) (pcmData.length & 0xFF);
        wav[41] = (byte) ((pcmData.length >> 8) & 0xFF);
        wav[42] = (byte) ((pcmData.length >> 16) & 0xFF);
        wav[43] = (byte) ((pcmData.length >> 24) & 0xFF);
        
        // Copy PCM data
        System.arraycopy(pcmData, 0, wav, 44, pcmData.length);
        
        return wav;
    }
}