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
        assertTrue(result.length >= 44); // Minimum WAV header size
        
        // Verify RIFF header
        assertEquals('R', result[0]);
        assertEquals('I', result[1]);
        assertEquals('F', result[2]);
        assertEquals('F', result[3]);
        
        // Verify WAVE format
        assertEquals('W', result[8]);
        assertEquals('A', result[9]);
        assertEquals('V', result[10]);
        assertEquals('E', result[11]);
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
     * Creates a minimal valid WAV header for testing
     */
    private byte[] createValidWavHeader() {
        byte[] header = new byte[44]; // Standard WAV header size
        
        // RIFF header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        
        // File size (36 bytes after this point)
        header[4] = 0x24;
        header[5] = 0x00;
        header[6] = 0x00;
        header[7] = 0x00;
        
        // WAVE format
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        
        // fmt sub-chunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        
        // Sub-chunk size (16 bytes)
        header[16] = 0x10;
        header[17] = 0x00;
        header[18] = 0x00;
        header[19] = 0x00;
        
        // Audio format (PCM = 1)
        header[20] = 0x01;
        header[21] = 0x00;
        
        // Number of channels (1)
        header[22] = 0x01;
        header[23] = 0x00;
        
        // Sample rate (44100 Hz)
        header[24] = 0x44;
        header[25] = (byte) 0xAC;
        header[26] = 0x00;
        header[27] = 0x00;
        
        // Byte rate (44100 * 1 * 2 = 88200)
        header[28] = (byte) 0x88;
        header[29] = 0x58;
        header[30] = 0x01;
        header[31] = 0x00;
        
        // Block align (1 * 2 = 2)
        header[32] = 0x02;
        header[33] = 0x00;
        
        // Bits per sample (16)
        header[34] = 0x10;
        header[35] = 0x00;
        
        // data sub-chunk
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        
        // Data size (0 for no audio data)
        header[40] = 0x00;
        header[41] = 0x00;
        header[42] = 0x00;
        header[43] = 0x00;
        
        return header;
    }
}