package com.tomasburgaleta.exampleia.domain.model;

import java.util.Objects;

/**
 * Domain entity to hold audio data and processing results
 */
public class AudioBean {

    private final byte[] audioData;
    private String transcribedText;
    private String aiResponse;
    private final String id;
    private long samplesPerSecond;
    private short bitsPerSample;
    private short channels;

    
    public AudioBean(String id, byte[] audioData) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.audioData = Objects.requireNonNull(audioData, "Audio data cannot be null");
    }

    public long getSamplesPerSecond() {
        return samplesPerSecond;
    }

    public void setSamplesPerSecond(long samplesPerSecond) {
        this.samplesPerSecond = samplesPerSecond;
    }

    public short getChannels() {
        return channels;
    }

    public void setChannels(short channels) {
        this.channels = channels;
    }

    public short getBitsPerSample() {
        return bitsPerSample;
    }

    public void setBitsPerSample(short bitsPerSample) {
        this.bitsPerSample = bitsPerSample;
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
    
    public String getAiResponse() {
        return aiResponse;
    }
    
    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }
    
    public boolean hasAiResponse() {
        return aiResponse != null && !aiResponse.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioBean audioBean = (AudioBean) o;
        return Objects.equals(id, audioBean.id);
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