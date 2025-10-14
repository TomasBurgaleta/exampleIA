package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Infrastructure adapter for storing audio recordings in memory
 * This adapter implements in-memory storage using a concurrent map
 */
@Component
public class InMemoryAudioRecordingAdapter implements AudioRecordingPort {
    
    private final ConcurrentHashMap<String, AudioBean> recordings = new ConcurrentHashMap<>();
    
    @Override
    public AudioBean storeRecording(AudioBean audioBean) {
        if (audioBean == null) {
            throw new IllegalArgumentException("AudioBean cannot be null");
        }
        
        if (audioBean.getId() == null || audioBean.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("AudioBean ID cannot be null or empty");
        }
        
        recordings.put(audioBean.getId(), audioBean);
        return audioBean;
    }
    
    @Override
    public AudioBean getRecording(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Recording ID cannot be null or empty");
        }
        
        return recordings.get(id);
    }
    
    @Override
    public boolean clearRecording(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Recording ID cannot be null or empty");
        }
        
        return recordings.remove(id) != null;
    }
}
