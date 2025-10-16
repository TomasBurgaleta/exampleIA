package com.tomasburgaleta.exampleia.infrastructure.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

class RmsSilenceDetectorAdapterTest {
    
    private RmsSilenceDetectorAdapter silenceDetector;
    
    @BeforeEach
    void setUp() {
        silenceDetector = new RmsSilenceDetectorAdapter();
    }
    
    @Test
    void testDetectSilence_NullData() {
        boolean result = silenceDetector.detectSilence(null, 44100, (short) 16, (short) 1);
        assertTrue(result, "Null data should be considered silent");
    }
    
    @Test
    void testDetectSilence_EmptyData() {
        byte[] emptyData = new byte[0];
        boolean result = silenceDetector.detectSilence(emptyData, 44100, (short) 16, (short) 1);
        assertTrue(result, "Empty data should be considered silent");
    }
    
    @Test
    void testDetectSilence_AllZeros16Bit() {
        // Create silent audio (all zeros)
        byte[] silentData = new byte[1000]; // 500 samples of 16-bit mono
        boolean result = silenceDetector.detectSilence(silentData, 44100, (short) 16, (short) 1);
        assertTrue(result, "All-zero data should be detected as silent");
    }
    
    @Test
    void testDetectSilence_LowAmplitude16Bit() {
        // Create very low amplitude audio (below threshold)
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < 500; i++) {
            // Very low amplitude values (below 1% of max)
            buffer.putShort((short) 100); // 100 / 32768 ≈ 0.003 (0.3%)
        }
        
        boolean result = silenceDetector.detectSilence(buffer.array(), 44100, (short) 16, (short) 1);
        assertTrue(result, "Low amplitude should be detected as silent");
    }
    
    @Test
    void testDetectSilence_HighAmplitude16Bit() {
        // Create audible audio with significant amplitude
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < 500; i++) {
            // Higher amplitude values (above threshold)
            buffer.putShort((short) 5000); // 5000 / 32768 ≈ 0.15 (15%)
        }
        
        boolean result = silenceDetector.detectSilence(buffer.array(), 44100, (short) 16, (short) 1);
        assertFalse(result, "High amplitude should not be detected as silent");
    }
    
    @Test
    void testDetectSilence_MixedAmplitude16Bit() {
        // Create audio with some silent and some audible parts
        ByteBuffer buffer = ByteBuffer.allocate(2000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // First 90% silent
        for (int i = 0; i < 900; i++) {
            buffer.putShort((short) 50);
        }
        
        // Last 10% audible
        for (int i = 0; i < 100; i++) {
            buffer.putShort((short) 5000);
        }
        
        boolean result = silenceDetector.detectSilence(buffer.array(), 44100, (short) 16, (short) 1);
        // Since 90% is silent but we need 95%, this should be detected as NOT silent
        assertFalse(result, "Audio with 10% audible content should not be silent");
    }
    
    @Test
    void testDetectSilence_MostlySilent16Bit() {
        // Create audio with 96% silent and 4% audible
        ByteBuffer buffer = ByteBuffer.allocate(2500);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // First 96% silent
        for (int i = 0; i < 1200; i++) {
            buffer.putShort((short) 50);
        }
        
        // Last 4% audible
        for (int i = 0; i < 50; i++) {
            buffer.putShort((short) 5000);
        }
        
        boolean result = silenceDetector.detectSilence(buffer.array(), 44100, (short) 16, (short) 1);
        assertTrue(result, "Audio with 96% silent content should be detected as silent");
    }
    
    @Test
    void testDetectSilence_8BitAudio() {
        // Create silent 8-bit audio
        byte[] silentData = new byte[1000];
        for (int i = 0; i < 1000; i++) {
            silentData[i] = (byte) 128; // 128 is zero for 8-bit unsigned audio
        }
        
        boolean result = silenceDetector.detectSilence(silentData, 44100, (short) 8, (short) 1);
        assertTrue(result, "Silent 8-bit audio should be detected as silent");
    }
    
    @Test
    void testDetectSilence_24BitAudio() {
        // Create silent 24-bit audio
        ByteBuffer buffer = ByteBuffer.allocate(3000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < 1000; i++) {
            // 24-bit zero
            buffer.put((byte) 0);
            buffer.put((byte) 0);
            buffer.put((byte) 0);
        }
        
        boolean result = silenceDetector.detectSilence(buffer.array(), 44100, (short) 24, (short) 1);
        assertTrue(result, "Silent 24-bit audio should be detected as silent");
    }
    
    @Test
    void testDetectSilence_Stereo() {
        // Create silent stereo audio (500 samples * 2 channels * 2 bytes per sample = 2000 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(2000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < 500; i++) {
            buffer.putShort((short) 0); // Left channel
            buffer.putShort((short) 0); // Right channel
        }
        
        boolean result = silenceDetector.detectSilence(buffer.array(), 44100, (short) 16, (short) 2);
        assertTrue(result, "Silent stereo audio should be detected as silent");
    }
    
    @Test
    void testDetectSilence_StereoOneChannelLoud() {
        // Create stereo audio with one loud channel (500 samples * 2 channels * 2 bytes = 2000 bytes)
        ByteBuffer buffer = ByteBuffer.allocate(2000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < 500; i++) {
            buffer.putShort((short) 5000); // Left channel - loud
            buffer.putShort((short) 0);    // Right channel - silent
        }
        
        boolean result = silenceDetector.detectSilence(buffer.array(), 44100, (short) 16, (short) 2);
        assertFalse(result, "Stereo with one loud channel should not be silent");
    }
}
