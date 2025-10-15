package com.tomasburgaleta.exampleia.integration;

import com.tomasburgaleta.exampleia.application.service.SilenceDetectionService;
import com.tomasburgaleta.exampleia.infrastructure.adapter.RmsSilenceDetectorAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for silence detection across application and infrastructure layers
 */
class SilenceDetectionIntegrationTest {
    
    private SilenceDetectionService silenceDetectionService;
    
    @BeforeEach
    void setUp() {
        // Wire up the real implementation
        RmsSilenceDetectorAdapter adapter = new RmsSilenceDetectorAdapter();
        silenceDetectionService = new SilenceDetectionService(adapter);
    }
    
    @Test
    void testIntegration_SilentAudio16Bit() {
        // Create completely silent 16-bit mono audio
        byte[] silentData = new byte[1000];
        
        boolean result = silenceDetectionService.isSilent(silentData, 44100, (short) 16, (short) 1);
        
        assertTrue(result, "Completely silent audio should be detected as silent");
    }
    
    @Test
    void testIntegration_AudibleAudio16Bit() {
        // Create audible 16-bit mono audio
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < 500; i++) {
            // Significant amplitude
            buffer.putShort((short) 5000);
        }
        
        boolean result = silenceDetectionService.isSilent(buffer.array(), 44100, (short) 16, (short) 1);
        
        assertFalse(result, "Audible audio should not be detected as silent");
    }
    
    @Test
    void testIntegration_MixedAudio16Bit() {
        // Create audio with 90% silent and 10% audible
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
        
        boolean result = silenceDetectionService.isSilent(buffer.array(), 44100, (short) 16, (short) 1);
        
        // With 90% silent but threshold at 95%, this should be NOT silent
        assertFalse(result, "Audio with 10% audible content should not be detected as silent");
    }
    
    @Test
    void testIntegration_StereoSilentAudio() {
        // Create silent stereo audio
        ByteBuffer buffer = ByteBuffer.allocate(2000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < 500; i++) {
            buffer.putShort((short) 0); // Left
            buffer.putShort((short) 0); // Right
        }
        
        boolean result = silenceDetectionService.isSilent(buffer.array(), 44100, (short) 16, (short) 2);
        
        assertTrue(result, "Silent stereo audio should be detected as silent");
    }
    
    @Test
    void testIntegration_StereoAudibleAudio() {
        // Create audible stereo audio
        ByteBuffer buffer = ByteBuffer.allocate(2000);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        for (int i = 0; i < 500; i++) {
            buffer.putShort((short) 5000); // Left
            buffer.putShort((short) 5000); // Right
        }
        
        boolean result = silenceDetectionService.isSilent(buffer.array(), 44100, (short) 16, (short) 2);
        
        assertFalse(result, "Audible stereo audio should not be detected as silent");
    }
    
    @Test
    void testIntegration_EmptyAudio() {
        byte[] emptyData = new byte[0];
        
        boolean result = silenceDetectionService.isSilent(emptyData, 44100, (short) 16, (short) 1);
        
        assertTrue(result, "Empty audio should be considered silent");
    }
    
    @Test
    void testIntegration_DifferentSampleRates() {
        // Create silent audio
        byte[] silentData = new byte[1000];
        
        // Test with different sample rates
        assertTrue(silenceDetectionService.isSilent(silentData, 8000, (short) 16, (short) 1));
        assertTrue(silenceDetectionService.isSilent(silentData, 16000, (short) 16, (short) 1));
        assertTrue(silenceDetectionService.isSilent(silentData, 44100, (short) 16, (short) 1));
        assertTrue(silenceDetectionService.isSilent(silentData, 48000, (short) 16, (short) 1));
    }
    
    @Test
    void testIntegration_RealWorldScenario() {
        // Simulate a real recording scenario:
        // - First second: silence (warming up microphone)
        // - Next two seconds: audible speech
        // - Last second: silence again
        
        int sampleRate = 44100;
        short bitDepth = 16;
        short channels = 1;
        
        // 4 seconds of audio at 44.1kHz, 16-bit, mono
        int totalSamples = sampleRate * 4;
        ByteBuffer buffer = ByteBuffer.allocate(totalSamples * 2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        
        // First second: silence
        for (int i = 0; i < sampleRate; i++) {
            buffer.putShort((short) 0);
        }
        
        // Next two seconds: audible
        for (int i = 0; i < sampleRate * 2; i++) {
            // Varying amplitude simulating speech
            short amplitude = (short) (3000 + Math.sin(i * 0.01) * 2000);
            buffer.putShort(amplitude);
        }
        
        // Last second: silence
        for (int i = 0; i < sampleRate; i++) {
            buffer.putShort((short) 0);
        }
        
        boolean result = silenceDetectionService.isSilent(buffer.array(), sampleRate, bitDepth, channels);
        
        // With 2 seconds audible out of 4 seconds (50% audible), 
        // and needing 95% silent, this should be NOT silent
        assertFalse(result, "Audio with 50% audible content should not be detected as silent");
    }
}
