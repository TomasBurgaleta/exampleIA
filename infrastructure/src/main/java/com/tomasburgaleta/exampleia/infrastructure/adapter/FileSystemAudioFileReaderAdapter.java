package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import com.tomasburgaleta.exampleia.domain.port.AudioFileReaderPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File system implementation of the AudioFileReaderPort
 * This adapter reads audio files from the file system and validates WAV format
 */
@Component
public class FileSystemAudioFileReaderAdapter implements AudioFileReaderPort {
    
    // WAV file header constants
    private static final byte[] RIFF_HEADER = {'R', 'I', 'F', 'F'};
    private static final byte[] WAVE_FORMAT = {'W', 'A', 'V', 'E'};
    private static final int WAV_HEADER_MIN_SIZE = 12; // Minimum size to check RIFF + size + WAVE
    
    @Override
    public byte[] readWavFile(String filePath) throws AudioFileException {
        Path path = Paths.get(filePath);
        
        // Check if file exists and is readable
        if (!Files.exists(path)) {
            throw new AudioFileException("File not found: " + filePath);
        }
        
        if (!Files.isReadable(path)) {
            throw new AudioFileException("File is not readable: " + filePath);
        }
        
        if (Files.isDirectory(path)) {
            throw new AudioFileException("Path points to a directory, not a file: " + filePath);
        }
        
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new AudioFileException("Failed to read file: " + filePath, e);
        }
        
        // Validate WAV format
        validateWavFormat(fileBytes, filePath);
        
        return fileBytes;
    }
    
    /**
     * Validates that the file has a proper WAV format by checking the header
     * 
     * @param fileBytes The file content as byte array
     * @param filePath The file path for error reporting
     * @throws AudioFileException if the file is not a valid WAV format
     */
    private void validateWavFormat(byte[] fileBytes, String filePath) throws AudioFileException {
        if (fileBytes.length < WAV_HEADER_MIN_SIZE) {
            throw new AudioFileException("File is too small to be a valid WAV file: " + filePath);
        }
        
        // Check RIFF header (bytes 0-3)
        for (int i = 0; i < RIFF_HEADER.length; i++) {
            if (fileBytes[i] != RIFF_HEADER[i]) {
                throw new AudioFileException("Invalid WAV format: missing RIFF header in file: " + filePath);
            }
        }
        
        // Check WAVE format identifier (bytes 8-11)
        for (int i = 0; i < WAVE_FORMAT.length; i++) {
            if (fileBytes[8 + i] != WAVE_FORMAT[i]) {
                throw new AudioFileException("Invalid WAV format: missing WAVE format identifier in file: " + filePath);
            }
        }
    }
}