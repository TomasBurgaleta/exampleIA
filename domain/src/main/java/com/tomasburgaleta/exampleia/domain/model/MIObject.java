package com.tomasburgaleta.exampleia.domain.model;

import java.util.Objects;

/**
 * Domain entity to hold audio data and processing results
 */
public class MIObject {
    
    private byte[] audioData;
    private String transcribedText;
    private String id;
    
    public MIObject(String id, byte[] audioData) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.audioData = Objects.requireNonNull(audioData, "Audio data cannot be null");
    }
    
    public String getId() {
        return id;
    }
    
    public byte[] getAudioData() {
        return audioData != null ? audioData.clone() : null;
    }
    
    public String getTranscribedText() {
        return transcribedText;
    }
    
    public void setTranscribedText(String transcribedText) {
        this.transcribedText = transcribedText;
    }
    
    public boolean hasTranscribedText() {
        return transcribedText != null && !transcribedText.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MIObject miObject = (MIObject) o;
        return Objects.equals(id, miObject.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "MIObject{" +
                "id='" + id + '\'' +
                ", hasAudioData=" + (audioData != null) +
                ", audioDataSize=" + (audioData != null ? audioData.length : 0) +
                ", hasTranscribedText=" + hasTranscribedText() +
                '}';
    }
}