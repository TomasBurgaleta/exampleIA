package com.tomasburgaleta.exampleia.web.config;

import com.tomasburgaleta.exampleia.application.service.AudioFileReaderService;
import com.tomasburgaleta.exampleia.application.service.AudioListenerService;
import com.tomasburgaleta.exampleia.application.service.AudioRecordingService;
import com.tomasburgaleta.exampleia.application.service.WavByteProcessingService;
import com.tomasburgaleta.exampleia.domain.port.AudioFileReaderPort;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;
import com.tomasburgaleta.exampleia.domain.port.WavByteReaderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for dependency injection following Clean Architecture
 */
@Configuration
public class ApplicationConfig {
    
    @Bean
    public AudioListenerService audioListenerService(AudioListenerPort audioListenerPort) {
        return new AudioListenerService(audioListenerPort);
    }
    
    @Bean
    public AudioFileReaderService audioFileReaderService(AudioFileReaderPort audioFileReaderPort) {
        return new AudioFileReaderService(audioFileReaderPort);
    }
    
    @Bean
    public WavByteProcessingService wavByteProcessingService(WavByteReaderPort wavByteReaderPort) {
        return new WavByteProcessingService(wavByteReaderPort);
    }
    
    @Bean
    public AudioRecordingService audioRecordingService(AudioRecordingPort audioRecordingPort, AudioListenerPort audioListenerPort) {
        return new AudioRecordingService(audioRecordingPort, audioListenerPort);
    }
}