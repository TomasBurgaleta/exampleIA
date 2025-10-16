package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.port.SilenceDetectorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of silence detection using RMS (Root Mean Square) amplitude analysis
 */
public class RmsSilenceDetectorAdapter implements SilenceDetectorPort {
    
    private static final Logger logger = LoggerFactory.getLogger(RmsSilenceDetectorAdapter.class);
    
    // Threshold for silence detection (RMS amplitude)
    // Values below this are considered silence
    private static final double SILENCE_THRESHOLD = 0.01; // 1% of maximum amplitude
    
    // Minimum percentage of silent samples to consider the whole audio as silent
    private static final double SILENT_SAMPLES_PERCENTAGE = 0.95; // 95% of samples must be silent
    
    @Override
    public boolean detectSilence(byte[] pcmData, long samplesPerSecond, short bitsPerSample, short channels) {
        if (pcmData == null || pcmData.length == 0) {
            return true;
        }
        
        logger.debug("Analyzing audio for silence: {} bytes, {} Hz, {} bits, {} channels", 
                     pcmData.length, samplesPerSecond, bitsPerSample, channels);
        
        int bytesPerSample = bitsPerSample / 8;
        int totalSamples = pcmData.length / (bytesPerSample * channels);
        
        if (totalSamples == 0) {
            return true;
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(pcmData);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        int silentSamples = 0;
        double sumSquares = 0.0;
        
        // Calculate RMS for each sample
        for (int i = 0; i < totalSamples; i++) {
            double sampleValue = 0.0;
            
            // Read all channels for this sample and calculate average
            for (int ch = 0; ch < channels; ch++) {
                double channelValue = readSample(buffer, bitsPerSample, bytesPerSample);
                sampleValue += Math.abs(channelValue);
            }
            
            sampleValue /= channels;
            
            // Check if this sample is silent
            if (sampleValue < SILENCE_THRESHOLD) {
                silentSamples++;
            }
            
            sumSquares += sampleValue * sampleValue;
        }
        
        // Calculate overall RMS
        double rms = Math.sqrt(sumSquares / totalSamples);
        
        // Calculate percentage of silent samples
        double silentPercentage = (double) silentSamples / totalSamples;
        
        boolean isSilent = silentPercentage >= SILENT_SAMPLES_PERCENTAGE;
        
        logger.debug("Silence detection result: RMS={}, Silent samples={}%, IsSilent={}", 
                     String.format("%.4f", rms), 
                     String.format("%.2f", silentPercentage * 100), 
                     isSilent);
        
        return isSilent;
    }
    
    /**
     * Reads a single sample from the buffer and normalizes it to [-1.0, 1.0]
     */
    private double readSample(ByteBuffer buffer, short bitsPerSample, int bytesPerSample) {
        double normalized;
        
        if (bitsPerSample == 8) {
            // 8-bit audio is unsigned (0-255)
            int sample = buffer.get() & 0xFF;
            normalized = (sample - 128) / 128.0;
        } else if (bitsPerSample == 16) {
            // 16-bit audio is signed (-32768 to 32767)
            short sample = buffer.getShort();
            normalized = sample / 32768.0;
        } else if (bitsPerSample == 24) {
            // 24-bit audio is signed
            byte b1 = buffer.get();
            byte b2 = buffer.get();
            byte b3 = buffer.get();
            int sample = ((b3 << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF));
            // Sign extend if negative
            if ((sample & 0x800000) != 0) {
                sample |= 0xFF000000;
            }
            normalized = sample / 8388608.0;
        } else {
            // Unsupported bit depth, skip bytes
            for (int i = 0; i < bytesPerSample; i++) {
                buffer.get();
            }
            normalized = 0.0;
        }
        
        return normalized;
    }
}
