package com.tomasburgaleta.exampleia.web.config;

import com.tomasburgaleta.exampleia.application.service.AudioFileReaderService;
import com.tomasburgaleta.exampleia.application.service.AudioListenerService;
import com.tomasburgaleta.exampleia.application.service.AudioRecordingService;
import com.tomasburgaleta.exampleia.application.service.AudioStreamingService;
import com.tomasburgaleta.exampleia.application.service.SilenceDetectionService;
import com.tomasburgaleta.exampleia.application.service.WavByteProcessingService;
import com.tomasburgaleta.exampleia.domain.port.AiServicePort;
import com.tomasburgaleta.exampleia.domain.port.AudioFileReaderPort;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;
import com.tomasburgaleta.exampleia.domain.port.SilenceDetectorPort;
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
    public SilenceDetectionService silenceDetectionService(SilenceDetectorPort silenceDetectorPort) {
        return new SilenceDetectionService(silenceDetectorPort);
    }
    
    @Bean
    public AudioRecordingService audioRecordingService(AudioRecordingPort audioRecordingPort, 
                                                       AudioListenerPort audioListenerPort,
                                                       SilenceDetectionService silenceDetectionService,
                                                       AiServicePort aiServicePort) {
        return new AudioRecordingService(audioRecordingPort, audioListenerPort, silenceDetectionService, aiServicePort);
    }
    
    @Bean
    public AudioStreamingService audioStreamingService() {
        return new AudioStreamingService();
    }
}