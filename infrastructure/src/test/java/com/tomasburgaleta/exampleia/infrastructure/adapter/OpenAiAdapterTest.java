package com.tomasburgaleta.exampleia.infrastructure.adapter;

import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.infrastructure.config.OpenAiConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiAdapterTest {
    
    @Mock
    private OpenAiConfig openAiConfig;
    
    @BeforeEach
    void setUp() {
        lenient().when(openAiConfig.getApiKey()).thenReturn("test-api-key");
        lenient().when(openAiConfig.getModel()).thenReturn("gpt-3.5-turbo");
        lenient().when(openAiConfig.getTemperature()).thenReturn(0.7);
        lenient().when(openAiConfig.getMaxTokens()).thenReturn(150);
        lenient().when(openAiConfig.getLogRequests()).thenReturn(false);
        lenient().when(openAiConfig.getLogResponses()).thenReturn(false);
        lenient().when(openAiConfig.isValid()).thenReturn(true);
    }
    
    @Test
    void testConstructorWithNullConfig() {
        assertThrows(NullPointerException.class, () -> new OpenAiAdapter(null));
    }
    
    @Test
    void testConstructorInitializesAdapter() {
        OpenAiAdapter adapter = new OpenAiAdapter(openAiConfig);
        assertNotNull(adapter);
    }
    
    @Test
    void testSendPromptWithNullPrompt() {
        OpenAiAdapter adapter = new OpenAiAdapter(openAiConfig);
        assertThrows(NullPointerException.class, () -> adapter.sendPrompt(null));
    }
    
    @Test
    void testSendPromptWithEmptyPrompt() {
        OpenAiAdapter adapter = new OpenAiAdapter(openAiConfig);
        assertThrows(AudioProcessingException.class, () -> adapter.sendPrompt(""));
        assertThrows(AudioProcessingException.class, () -> adapter.sendPrompt("   "));
    }
    
    @Test
    void testSendPromptWithInvalidConfig() {
        when(openAiConfig.isValid()).thenReturn(false);
        OpenAiAdapter adapter = new OpenAiAdapter(openAiConfig);
        
        AudioProcessingException exception = assertThrows(AudioProcessingException.class, 
            () -> adapter.sendPrompt("Test prompt"));
        
        assertTrue(exception.getMessage().contains("OpenAI configuration is invalid"));
    }
}
