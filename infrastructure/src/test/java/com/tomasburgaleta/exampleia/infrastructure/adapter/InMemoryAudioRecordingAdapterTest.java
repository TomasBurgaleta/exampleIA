package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryAudioRecordingAdapterTest {
    
    private InMemoryAudioRecordingAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new InMemoryAudioRecordingAdapter();
    }
    
    @Test
    void testStoreRecording_Success() {
        // Arrange
        String id = "test-id";
        byte[] pcmData = new byte[]{1, 2, 3, 4, 5};
        AudioBean audioBean = new AudioBean(id, pcmData);
        audioBean.setSamplesPerSecond(44100);
        audioBean.setBitsPerSample((short) 16);
        audioBean.setChannels((short) 2);
        
        // Act
        AudioBean result = adapter.storeRecording(audioBean);
        
        // Assert
        assertNotNull(result);
        assertEquals(audioBean, result);
        
        // Verify it's actually stored
        AudioBean retrieved = adapter.getRecording(id);
        assertEquals(audioBean, retrieved);
    }
    
    @Test
    void testStoreRecording_NullBean() {
        assertThrows(IllegalArgumentException.class, () -> 
            adapter.storeRecording(null));
    }
    
    @Test
    void testStoreRecording_NullId() {
        // AudioBean constructor throws NullPointerException for null ID
        assertThrows(NullPointerException.class, () -> 
            new AudioBean(null, new byte[]{1, 2, 3}));
    }
    
    @Test
    void testGetRecording_Success() {
        // Arrange
        String id = "test-id";
        AudioBean audioBean = new AudioBean(id, new byte[]{1, 2, 3});
        adapter.storeRecording(audioBean);
        
        // Act
        AudioBean result = adapter.getRecording(id);
        
        // Assert
        assertNotNull(result);
        assertEquals(audioBean, result);
    }
    
    @Test
    void testGetRecording_NotFound() {
        // Act
        AudioBean result = adapter.getRecording("non-existent-id");
        
        // Assert
        assertNull(result);
    }
    
    @Test
    void testGetRecording_NullId() {
        assertThrows(IllegalArgumentException.class, () -> 
            adapter.getRecording(null));
    }
    
    @Test
    void testGetRecording_EmptyId() {
        assertThrows(IllegalArgumentException.class, () -> 
            adapter.getRecording(""));
    }
    
    @Test
    void testClearRecording_Success() {
        // Arrange
        String id = "test-id";
        AudioBean audioBean = new AudioBean(id, new byte[]{1, 2, 3});
        adapter.storeRecording(audioBean);
        
        // Act
        boolean result = adapter.clearRecording(id);
        
        // Assert
        assertTrue(result);
        
        // Verify it's actually removed
        AudioBean retrieved = adapter.getRecording(id);
        assertNull(retrieved);
    }
    
    @Test
    void testClearRecording_NotFound() {
        // Act
        boolean result = adapter.clearRecording("non-existent-id");
        
        // Assert
        assertFalse(result);
    }
    
    @Test
    void testClearRecording_NullId() {
        assertThrows(IllegalArgumentException.class, () -> 
            adapter.clearRecording(null));
    }
    
    @Test
    void testClearRecording_EmptyId() {
        assertThrows(IllegalArgumentException.class, () -> 
            adapter.clearRecording(""));
    }
    
    @Test
    void testMultipleRecordings() {
        // Arrange
        AudioBean bean1 = new AudioBean("id1", new byte[]{1, 2, 3});
        AudioBean bean2 = new AudioBean("id2", new byte[]{4, 5, 6});
        AudioBean bean3 = new AudioBean("id3", new byte[]{7, 8, 9});
        
        // Act
        adapter.storeRecording(bean1);
        adapter.storeRecording(bean2);
        adapter.storeRecording(bean3);
        
        // Assert
        assertEquals(bean1, adapter.getRecording("id1"));
        assertEquals(bean2, adapter.getRecording("id2"));
        assertEquals(bean3, adapter.getRecording("id3"));
        
        // Clear one
        assertTrue(adapter.clearRecording("id2"));
        
        // Verify only id2 is removed
        assertEquals(bean1, adapter.getRecording("id1"));
        assertNull(adapter.getRecording("id2"));
        assertEquals(bean3, adapter.getRecording("id3"));
    }
}
