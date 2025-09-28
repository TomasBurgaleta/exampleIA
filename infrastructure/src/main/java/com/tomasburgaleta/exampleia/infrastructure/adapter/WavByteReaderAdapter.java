package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioFileException;
import com.tomasburgaleta.exampleia.domain.port.WavByteReaderPort;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Infrastructure adapter for processing WAV byte arrays
 * This adapter validates WAV format and extracts metadata and PCM data
 */
@Component
public class WavByteReaderAdapter implements WavByteReaderPort {
    
    // WAV file header constants
    private static final byte[] RIFF_HEADER = {'R', 'I', 'F', 'F'};
    private static final byte[] WAVE_FORMAT = {'W', 'A', 'V', 'E'};
    private static final byte[] FMT_CHUNK = {'f', 'm', 't', ' '};
    private static final int WAV_HEADER_MIN_SIZE = 12; // Minimum size to check RIFF + size + WAVE
    private static final int FMT_CHUNK_SIZE = 16; // Standard PCM format chunk size
    
    @Override
    public AudioBean processWavBytes(byte[] wavBytes, String id) throws AudioFileException {
        if (wavBytes == null || wavBytes.length == 0) {
            throw new IllegalArgumentException("WAV bytes cannot be null or empty");
        }
        
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID cannot be null or empty");
        }
        
        // Validate WAV format
        validateWavFormat(wavBytes);
        
        // Extract WAV metadata
        WavMetadata metadata = extractWavMetadata(wavBytes);
        
        // Extract PCM data (audio data without header)
        byte[] pcmData = extractPcmFromWav(wavBytes);
        
        // Create AudioBean with extracted data
        AudioBean audioBean = new AudioBean(id, pcmData);
        audioBean.setSamplesPerSecond(metadata.sampleRate);
        audioBean.setBitsPerSample(metadata.bitsPerSample);
        audioBean.setChannels(metadata.channels);
        
        return audioBean;
    }
    
    /**
     * Validates that the byte array has a proper WAV format by checking the header
     * 
     * @param wavBytes The WAV file content as byte array
     * @throws AudioFileException if the byte array is not a valid WAV format
     */
    private void validateWavFormat(byte[] wavBytes) throws AudioFileException {
        if (wavBytes.length < WAV_HEADER_MIN_SIZE) {
            throw new AudioFileException("Data is too small to be a valid WAV file");
        }
        
        // Check RIFF header (bytes 0-3)
        for (int i = 0; i < RIFF_HEADER.length; i++) {
            if (wavBytes[i] != RIFF_HEADER[i]) {
                throw new AudioFileException("Invalid WAV format: missing RIFF header");
            }
        }
        
        // Check WAVE format identifier (bytes 8-11)
        for (int i = 0; i < WAVE_FORMAT.length; i++) {
            if (wavBytes[8 + i] != WAVE_FORMAT[i]) {
                throw new AudioFileException("Invalid WAV format: missing WAVE format identifier");
            }
        }
    }
    
    /**
     * Extracts WAV metadata from the format chunk
     * 
     * @param wavBytes The WAV file content as byte array
     * @return WavMetadata containing sample rate, bits per sample, and channels
     * @throws AudioFileException if the format chunk is not found or invalid
     */
    private WavMetadata extractWavMetadata(byte[] wavBytes) throws AudioFileException {
        int offset = 12; // Start after RIFF header (4) + size (4) + WAVE (4)
        
        // Look for the fmt chunk
        while (offset < wavBytes.length - 8) {
            String chunkId = new String(wavBytes, offset, 4);
            int chunkSize = readLittleEndianInt(wavBytes, offset + 4);
            
            if ("fmt ".equals(chunkId)) {
                if (chunkSize < 16) {
                    throw new AudioFileException("Invalid fmt chunk size");
                }
                
                // Extract audio format parameters
                int audioFormat = readLittleEndianShort(wavBytes, offset + 8);
                if (audioFormat != 1) { // PCM format
                    throw new AudioFileException("Only PCM format is supported");
                }
                
                short channels = readLittleEndianShort(wavBytes, offset + 10);
                int sampleRate = readLittleEndianInt(wavBytes, offset + 12);
                // Skip byte rate (4 bytes) and block align (2 bytes)
                short bitsPerSample = readLittleEndianShort(wavBytes, offset + 22);
                
                return new WavMetadata(sampleRate, bitsPerSample, channels);
            }
            
            offset += 8 + chunkSize;
        }
        
        throw new AudioFileException("fmt chunk not found in WAV file");
    }
    
    /**
     * Extracts PCM audio data from the WAV file (without header)
     * 
     * @param wavBytes The WAV file content as byte array
     * @return byte array containing only the PCM audio data
     * @throws AudioFileException if the data chunk is not found
     */
    private byte[] extractPcmFromWav(byte[] wavBytes) throws AudioFileException {
        int offset = 12; // Start after RIFF header
        
        while (offset < wavBytes.length - 8) {
            String chunkId = new String(wavBytes, offset, 4);
            int chunkSize = readLittleEndianInt(wavBytes, offset + 4);
            
            if ("data".equals(chunkId)) {
                // Found the data chunk containing PCM samples
                if (offset + 8 + chunkSize > wavBytes.length) {
                    throw new AudioFileException("Invalid data chunk size");
                }
                return Arrays.copyOfRange(wavBytes, offset + 8, offset + 8 + chunkSize);
            }
            
            offset += 8 + chunkSize;
        }
        
        throw new AudioFileException("data chunk not found in WAV file");
    }
    
    /**
     * Reads a little-endian 32-bit integer from byte array
     */
    private int readLittleEndianInt(byte[] data, int offset) {
        return (data[offset] & 0xFF) |
               ((data[offset + 1] & 0xFF) << 8) |
               ((data[offset + 2] & 0xFF) << 16) |
               ((data[offset + 3] & 0xFF) << 24);
    }
    
    /**
     * Reads a little-endian 16-bit short from byte array
     */
    private short readLittleEndianShort(byte[] data, int offset) {
        return (short) ((data[offset] & 0xFF) |
                       ((data[offset + 1] & 0xFF) << 8));
    }
    
    /**
     * Internal class to hold WAV metadata
     */
    private static class WavMetadata {
        final long sampleRate;
        final short bitsPerSample;
        final short channels;
        
        WavMetadata(long sampleRate, short bitsPerSample, short channels) {
            this.sampleRate = sampleRate;
            this.bitsPerSample = bitsPerSample;
            this.channels = channels;
        }
    }
}