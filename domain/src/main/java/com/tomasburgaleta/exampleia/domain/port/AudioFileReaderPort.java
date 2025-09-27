package com.tomasburgaleta.exampleia.domain.port;

/**
 * Port (interface) for audio file reading services
 * Following hexagonal architecture, this is the contract that infrastructure adapters must implement
 */
public interface AudioFileReaderPort {
    
    /**
     * Reads a WAV audio file from the specified file path and converts it to a byte array
     * 
     * @param filePath The absolute or relative path to the WAV file to read
     * @return The audio file content as byte array
     * @throws AudioFileException if the file doesn't exist, is not readable, or is not a valid WAV format
     */
    byte[] readWavFile(String filePath) throws AudioFileException;
}