package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileSystemAudioFileReaderAdapter
 */
class FileSystemAudioFileReaderAdapterTest {
    
    private FileSystemAudioFileReaderAdapter adapter;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        adapter = new FileSystemAudioFileReaderAdapter();
    }
    
    @Test
    void readWavFile_shouldReturnBytes_whenValidWavFile() throws IOException, AudioFileException {
        // Arrange
        byte[] validWav = createValidWavHeader();
        Path wavFile = tempDir.resolve("test.wav");
        Files.write(wavFile, validWav);
        
        // Act
        byte[] result = adapter.readWavFile(wavFile.toString());
        
        // Assert - result should be the PCM data only (8 bytes in our test case)
        assertNotNull(result);
        assertEquals(8, result.length);
    }
    
    @Test
    void readWavFile_shouldThrowException_whenFileDoesNotExist() {
        // Arrange
        String nonExistentPath = tempDir.resolve("nonexistent.wav").toString();
        
        // Act & Assert
        AudioFileException exception = assertThrows(AudioFileException.class, 
            () -> adapter.readWavFile(nonExistentPath));
        
        assertTrue(exception.getMessage().contains("File not found"));
    }
    
    @Test
    void readWavFile_shouldThrowException_whenPathIsDirectory() throws IOException {
        // Arrange
        Path directory = tempDir.resolve("directory");
        Files.createDirectory(directory);
        
        // Act & Assert
        AudioFileException exception = assertThrows(AudioFileException.class, 
            () -> adapter.readWavFile(directory.toString()));
        
        assertTrue(exception.getMessage().contains("Path points to a directory"));
    }
    
    @Test
    void readWavFile_shouldThrowException_whenFileTooSmall() throws IOException {
        // Arrange
        byte[] tooSmallContent = new byte[5]; // Less than minimum WAV header size
        Path smallFile = tempDir.resolve("small.wav");
        Files.write(smallFile, tooSmallContent);
        
        // Act & Assert
        AudioFileException exception = assertThrows(AudioFileException.class, 
            () -> adapter.readWavFile(smallFile.toString()));
        
        assertTrue(exception.getMessage().contains("File is too small"));
    }
    
    @Test
    void readWavFile_shouldThrowException_whenMissingRiffHeader() throws IOException {
        // Arrange
        byte[] invalidHeader = new byte[12];
        invalidHeader[0] = 'X'; // Should be 'R'
        invalidHeader[1] = 'I';
        invalidHeader[2] = 'F';
        invalidHeader[3] = 'F';
        // Add WAVE part
        invalidHeader[8] = 'W';
        invalidHeader[9] = 'A';
        invalidHeader[10] = 'V';
        invalidHeader[11] = 'E';
        
        Path invalidFile = tempDir.resolve("invalid.wav");
        Files.write(invalidFile, invalidHeader);
        
        // Act & Assert
        AudioFileException exception = assertThrows(AudioFileException.class, 
            () -> adapter.readWavFile(invalidFile.toString()));
        
        assertTrue(exception.getMessage().contains("missing RIFF header"));
    }
    
    @Test
    void readWavFile_shouldThrowException_whenMissingWaveFormat() throws IOException {
        // Arrange
        byte[] invalidHeader = new byte[12];
        // Add RIFF part
        invalidHeader[0] = 'R';
        invalidHeader[1] = 'I';
        invalidHeader[2] = 'F';
        invalidHeader[3] = 'F';
        // Invalid WAVE part
        invalidHeader[8] = 'X'; // Should be 'W'
        invalidHeader[9] = 'A';
        invalidHeader[10] = 'V';
        invalidHeader[11] = 'E';
        
        Path invalidFile = tempDir.resolve("invalid.wav");
        Files.write(invalidFile, invalidHeader);
        
        // Act & Assert
        AudioFileException exception = assertThrows(AudioFileException.class, 
            () -> adapter.readWavFile(invalidFile.toString()));
        
        assertTrue(exception.getMessage().contains("missing WAVE format identifier"));
    }
    
    /**
     * Creates a minimal valid WAV header with PCM data for testing
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
        
        // fmt subchunk
        wav[12] = 'f';
        wav[13] = 'm';
        wav[14] = 't';
        wav[15] = ' ';
        
        // fmt chunk size (16)
        wav[16] = 0x10;
        wav[17] = 0x00;
        wav[18] = 0x00;
        wav[19] = 0x00;
        
        // Audio format (PCM = 1)
        wav[20] = 0x01;
        wav[21] = 0x00;
        
        // Number of channels (2)
        wav[22] = 0x02;
        wav[23] = 0x00;
        
        // Sample rate (44100 Hz)
        wav[24] = 0x44;
        wav[25] = (byte) 0xAC;
        wav[26] = 0x00;
        wav[27] = 0x00;
        
        // Byte rate (44100 * 2 * 2 = 176400)
        wav[28] = (byte) 0x10;
        wav[29] = (byte) 0xB1;
        wav[30] = 0x02;
        wav[31] = 0x00;
        
        // Block align (2 * 2 = 4)
        wav[32] = 0x04;
        wav[33] = 0x00;
        
        // Bits per sample (16)
        wav[34] = 0x10;
        wav[35] = 0x00;
        
        // data subchunk
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