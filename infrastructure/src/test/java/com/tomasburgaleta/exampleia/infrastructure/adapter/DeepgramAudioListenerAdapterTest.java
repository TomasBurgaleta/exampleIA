package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.infrastructure.config.DeepgramSpeechConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeepgramAudioListenerAdapterTest {

    @Mock
    private DeepgramSpeechConfig deepgramConfig;

    @Mock
    private RestTemplate restTemplate;

    private DeepgramAudioListenerAdapter adapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adapter = new DeepgramAudioListenerAdapter(deepgramConfig, restTemplate);
    }

    @Test
    void shouldTranscribeAudioSuccessfully() throws AudioProcessingException {
        // Given
        byte[] audioData = new byte[]{1, 2, 3, 4};
        AudioBean audioBean = new AudioBean("test-id", audioData);
        audioBean.setSamplesPerSecond(16000);
        audioBean.setBitsPerSample((short) 16);
        audioBean.setChannels((short) 1);

        when(deepgramConfig.isValid()).thenReturn(true);
        when(deepgramConfig.getApiKey()).thenReturn("test-api-key");
        when(deepgramConfig.getModel()).thenReturn("nova-2");
        when(deepgramConfig.getLanguage()).thenReturn("es");

        Map<String, Object> mockResponse = createMockResponse("Hola mundo", "es");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // When
        byte[] result = adapter.transcribe(audioBean);

        // Then
        assertNotNull(result);
        assertEquals("Hola mundo", audioBean.getTranscribedText());
        assertEquals("es", audioBean.getDetectedLanguage());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }

    @Test
    void shouldThrowExceptionWhenConfigurationIsInvalid() {
        // Given
        byte[] audioData = new byte[]{1, 2, 3, 4};
        AudioBean audioBean = new AudioBean("test-id", audioData);
        audioBean.setSamplesPerSecond(16000);
        audioBean.setBitsPerSample((short) 16);
        audioBean.setChannels((short) 1);

        when(deepgramConfig.isValid()).thenReturn(false);

        // When & Then
        assertThrows(AudioProcessingException.class, () -> adapter.transcribe(audioBean));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldThrowExceptionWhenAudioDataIsEmpty() {
        // Given
        AudioBean audioBean = new AudioBean("test-id", new byte[]{});
        when(deepgramConfig.isValid()).thenReturn(true);

        // When & Then
        assertThrows(AudioProcessingException.class, () -> adapter.transcribe(audioBean));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldThrowExceptionWhenAudioBeanIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> adapter.transcribe(null));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void shouldHandleEmptyTranscriptionResponse() throws AudioProcessingException {
        // Given
        byte[] audioData = new byte[]{1, 2, 3, 4};
        AudioBean audioBean = new AudioBean("test-id", audioData);
        audioBean.setSamplesPerSecond(16000);
        audioBean.setBitsPerSample((short) 16);
        audioBean.setChannels((short) 1);

        when(deepgramConfig.isValid()).thenReturn(true);
        when(deepgramConfig.getApiKey()).thenReturn("test-api-key");
        when(deepgramConfig.getModel()).thenReturn("nova-2");
        when(deepgramConfig.getLanguage()).thenReturn("es");

        Map<String, Object> mockResponse = createMockResponse("", "es");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // When
        byte[] result = adapter.transcribe(audioBean);

        // Then
        assertNotNull(result);
        assertEquals("", audioBean.getTranscribedText());
        assertEquals("es", audioBean.getDetectedLanguage());
    }

    @Test
    void shouldThrowExceptionWhenRestTemplateThrowsException() {
        // Given
        byte[] audioData = new byte[]{1, 2, 3, 4};
        AudioBean audioBean = new AudioBean("test-id", audioData);
        audioBean.setSamplesPerSecond(16000);
        audioBean.setBitsPerSample((short) 16);
        audioBean.setChannels((short) 1);

        when(deepgramConfig.isValid()).thenReturn(true);
        when(deepgramConfig.getApiKey()).thenReturn("test-api-key");
        when(deepgramConfig.getModel()).thenReturn("nova-2");
        when(deepgramConfig.getLanguage()).thenReturn("es");

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenThrow(new RestClientException("API error"));

        // When & Then
        assertThrows(AudioProcessingException.class, () -> adapter.transcribe(audioBean));
    }

    @Test
    void shouldValidateAudioMetadata() {
        // Given - invalid samples per second
        byte[] audioData = new byte[]{1, 2, 3, 4};
        AudioBean audioBean = new AudioBean("test-id", audioData);
        audioBean.setSamplesPerSecond(0);
        audioBean.setBitsPerSample((short) 16);
        audioBean.setChannels((short) 1);

        when(deepgramConfig.isValid()).thenReturn(true);
        when(deepgramConfig.getApiKey()).thenReturn("test-api-key");

        // When & Then
        assertThrows(AudioProcessingException.class, () -> adapter.transcribe(audioBean));
    }

    @Test
    void shouldDelegateListenAudioToTranscribe() throws AudioProcessingException {
        // Given
        byte[] audioData = new byte[]{1, 2, 3, 4};
        AudioBean audioBean = new AudioBean("test-id", audioData);
        audioBean.setSamplesPerSecond(16000);
        audioBean.setBitsPerSample((short) 16);
        audioBean.setChannels((short) 1);

        when(deepgramConfig.isValid()).thenReturn(true);
        when(deepgramConfig.getApiKey()).thenReturn("test-api-key");
        when(deepgramConfig.getModel()).thenReturn("nova-2");
        when(deepgramConfig.getLanguage()).thenReturn("es");

        Map<String, Object> mockResponse = createMockResponse("Test", "es");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(responseEntity);

        // When
        byte[] result = adapter.listenAudio(audioBean);

        // Then
        assertNotNull(result);
        assertEquals("Test", audioBean.getTranscribedText());
    }

    @Test
    void shouldThrowExceptionWhenDeepgramConfigIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new DeepgramAudioListenerAdapter(null, restTemplate));
    }

    @Test
    void shouldThrowExceptionWhenRestTemplateIsNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> new DeepgramAudioListenerAdapter(deepgramConfig, null));
    }

    private Map<String, Object> createMockResponse(String transcript, String language) {
        Map<String, Object> alternative = new HashMap<>();
        alternative.put("transcript", transcript);

        Map<String, Object> channel = new HashMap<>();
        channel.put("alternatives", List.of(alternative));
        channel.put("detected_language", language);

        Map<String, Object> results = new HashMap<>();
        results.put("channels", List.of(channel));
        results.put("language", language);

        Map<String, Object> response = new HashMap<>();
        response.put("results", results);

        return response;
    }
}
