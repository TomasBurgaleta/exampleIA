package com.tomasburgaleta.exampleia.infrastructure.config;

import com.tomasburgaleta.exampleia.domain.port.AudioFileReaderPort;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;
import com.tomasburgaleta.exampleia.domain.port.SilenceDetectorPort;
import com.tomasburgaleta.exampleia.domain.port.WavByteReaderPort;
import com.tomasburgaleta.exampleia.infrastructure.adapter.AzureAudioListenerAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.FileSystemAudioFileReaderAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.InMemoryAudioRecordingAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.RmsSilenceDetectorAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.WavByteReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for infrastructure adapters
 */
@Configuration
public class InfrastructureConfig {
    
    @Bean
    public AudioListenerPort audioListenerPort(AzureSpeechConfig azureSpeechConfig) {
        return new AzureAudioListenerAdapter(azureSpeechConfig);
    }
    
    @Bean
    public AudioFileReaderPort audioFileReaderPort() {
        return new FileSystemAudioFileReaderAdapter();
    }
    
    @Bean
    public WavByteReaderPort wavByteReaderPort() {
        return new WavByteReaderAdapter();
    }
    
    @Bean
    public AudioRecordingPort audioRecordingPort() {
        return new InMemoryAudioRecordingAdapter();
    }
    
    @Bean
    public SilenceDetectorPort silenceDetectorPort() {
        return new RmsSilenceDetectorAdapter();
    }
}
