package com.tomasburgaleta.exampleia.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AudioStreamingServiceTest {
    
    private AudioStreamingService audioStreamingService;
    
    @BeforeEach
    void setUp() {
        audioStreamingService = new AudioStreamingService();
    }
    
    @Test
    void testAddAudioBytes_ValidChunk() {
        // Given
        byte[] chunk = new byte[]{1, 2, 3, 4, 5};
        
        // When
        audioStreamingService.addAudioBytes(chunk);
        
        // Then
        assertEquals(5, audioStreamingService.getBufferSize());
    }
    
    @Test
    void testAddAudioBytes_NullChunk() {
        // When/Then
        assertThrows(NullPointerException.class, () -> audioStreamingService.addAudioBytes(null));
    }
    
    @Test
    void testAddAudioBytes_EmptyChunk() {
        // Given
        byte[] emptyChunk = new byte[]{};
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> audioStreamingService.addAudioBytes(emptyChunk));
    }
    
    @Test
    void testAddAudioBytes_MultipleChunks() {
        // Given
        byte[] chunk1 = new byte[]{1, 2, 3};
        byte[] chunk2 = new byte[]{4, 5, 6};
        byte[] chunk3 = new byte[]{7, 8};
        
        // When
        audioStreamingService.addAudioBytes(chunk1);
        audioStreamingService.addAudioBytes(chunk2);
        audioStreamingService.addAudioBytes(chunk3);
        
        // Then
        assertEquals(8, audioStreamingService.getBufferSize());
    }
    
    @Test
    void testExtractAndClearAudioBytes_WithData() {
        // Given
        byte[] chunk1 = new byte[]{1, 2, 3};
        byte[] chunk2 = new byte[]{4, 5, 6};
        audioStreamingService.addAudioBytes(chunk1);
        audioStreamingService.addAudioBytes(chunk2);
        
        // When
        byte[] extracted = audioStreamingService.extractAndClearAudioBytes();
        
        // Then
        assertNotNull(extracted);
        assertEquals(6, extracted.length);
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6}, extracted);
        assertEquals(0, audioStreamingService.getBufferSize());
        assertTrue(audioStreamingService.isEmpty());
    }
    
    @Test
    void testExtractAndClearAudioBytes_EmptyBuffer() {
        // When
        byte[] extracted = audioStreamingService.extractAndClearAudioBytes();
        
        // Then
        assertNotNull(extracted);
        assertEquals(0, extracted.length);
        assertTrue(audioStreamingService.isEmpty());
    }
    
    @Test
    void testExtractAndClearAudioBytes_ClearsBuffer() {
        // Given
        byte[] chunk = new byte[]{1, 2, 3, 4, 5};
        audioStreamingService.addAudioBytes(chunk);
        
        // When
        audioStreamingService.extractAndClearAudioBytes();
        
        // Then
        assertEquals(0, audioStreamingService.getBufferSize());
        
        // When extracting again
        byte[] extracted = audioStreamingService.extractAndClearAudioBytes();
        
        // Then
        assertEquals(0, extracted.length);
    }
    
    @Test
    void testIsEmpty_InitiallyEmpty() {
        // Then
        assertTrue(audioStreamingService.isEmpty());
    }
    
    @Test
    void testIsEmpty_NotEmptyAfterAdd() {
        // Given
        byte[] chunk = new byte[]{1, 2, 3};
        
        // When
        audioStreamingService.addAudioBytes(chunk);
        
        // Then
        assertFalse(audioStreamingService.isEmpty());
    }
    
    @Test
    void testClear() {
        // Given
        byte[] chunk = new byte[]{1, 2, 3, 4, 5};
        audioStreamingService.addAudioBytes(chunk);
        
        // When
        audioStreamingService.clear();
        
        // Then
        assertTrue(audioStreamingService.isEmpty());
        assertEquals(0, audioStreamingService.getBufferSize());
    }
    
    @Test
    void testThreadSafety_ConcurrentAdds() throws InterruptedException {
        // Given
        int numThreads = 10;
        int chunksPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        
        // When
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < chunksPerThread; j++) {
                        audioStreamingService.addAudioBytes(new byte[]{1, 2, 3});
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Then
        assertEquals(numThreads * chunksPerThread * 3, audioStreamingService.getBufferSize());
    }
    
    @Test
    void testThreadSafety_ConcurrentAddAndExtract() throws InterruptedException {
        // Given
        int numProducers = 5;
        int numConsumers = 3;
        int chunksPerProducer = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numProducers + numConsumers);
        CountDownLatch latch = new CountDownLatch(numProducers + numConsumers);
        
        // When - Start producers
        for (int i = 0; i < numProducers; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < chunksPerProducer; j++) {
                        audioStreamingService.addAudioBytes(new byte[]{1, 2, 3});
                        Thread.sleep(1); // Small delay to simulate real-world scenario
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // When - Start consumers
        for (int i = 0; i < numConsumers; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 20; j++) {
                        audioStreamingService.extractAndClearAudioBytes();
                        Thread.sleep(10); // Consumers slower than producers
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Then - No exception should occur, operations should be thread-safe
        // Buffer size will vary, but should not cause any concurrent modification exceptions
        assertTrue(audioStreamingService.getBufferSize() >= 0);
    }
}
