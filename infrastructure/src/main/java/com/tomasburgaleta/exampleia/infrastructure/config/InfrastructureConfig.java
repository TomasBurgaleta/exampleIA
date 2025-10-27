package com.tomasburgaleta.exampleia.infrastructure.config;

import com.tomasburgaleta.exampleia.domain.port.AiServicePort;
import com.tomasburgaleta.exampleia.domain.port.AudioFileReaderPort;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;
import com.tomasburgaleta.exampleia.domain.port.SilenceDetectorPort;
import com.tomasburgaleta.exampleia.domain.port.SpeechToTextPort;
import com.tomasburgaleta.exampleia.domain.port.WavByteReaderPort;
import com.tomasburgaleta.exampleia.infrastructure.adapter.AzureAudioListenerAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.DeepgramAudioListenerAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.FileSystemAudioFileReaderAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.InMemoryAudioRecordingAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.OpenAiAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.RmsSilenceDetectorAdapter;
import com.tomasburgaleta.exampleia.infrastructure.adapter.WavByteReaderAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for infrastructure adapters
 */
@Configuration
public class InfrastructureConfig {
    
    @Value("${speech.provider:azure}")
    private String speechProvider;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public SpeechToTextPort speechToTextPort(AzureSpeechConfig azureSpeechConfig, 
                                             DeepgramSpeechConfig deepgramSpeechConfig,
                                             RestTemplate restTemplate) {
        if ("deepgram".equalsIgnoreCase(speechProvider)) {
            return new DeepgramAudioListenerAdapter(deepgramSpeechConfig, restTemplate);
        } else {
            // Default to Azure
            return new AzureAudioListenerAdapter(azureSpeechConfig);
        }
    }
    
    @Bean
    public AudioListenerPort audioListenerPort(SpeechToTextPort speechToTextPort) {
        // AudioListenerPort is now delegated to SpeechToTextPort
        return (AudioListenerPort) speechToTextPort;
    }
    
    @Bean
    public AiServicePort aiServicePort(OpenAiConfig openAiConfig) {
        return new OpenAiAdapter(openAiConfig);
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
