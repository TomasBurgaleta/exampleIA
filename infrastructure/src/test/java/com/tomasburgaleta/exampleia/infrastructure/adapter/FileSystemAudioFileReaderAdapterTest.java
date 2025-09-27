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
        byte[] validWavHeader = createValidWavHeader();
        Path wavFile = tempDir.resolve("test.wav");
        Files.write(wavFile, validWavHeader);
        
        // Act
        byte[] result = adapter.readWavFile(wavFile.toString());
        
        // Assert
        assertArrayEquals(validWavHeader, result);
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
     * Creates a minimal valid WAV header for testing
     */
    private byte[] createValidWavHeader() {
        byte[] header = new byte[44]; // Standard WAV header size
        
        // RIFF header
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        
        // File size (dummy value)
        header[4] = 0x24; // 36 bytes after this point
        header[5] = 0x00;
        header[6] = 0x00;
        header[7] = 0x00;
        
        // WAVE format
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        
        // Rest of the header with minimal valid values
        // fmt subchunk
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        
        return header;
    }
}