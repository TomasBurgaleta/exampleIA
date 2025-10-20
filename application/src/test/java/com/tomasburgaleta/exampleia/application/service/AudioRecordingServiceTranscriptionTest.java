package com.tomasburgaleta.exampleia.application.service;

import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.domain.port.AiServicePort;
import com.tomasburgaleta.exampleia.domain.port.AudioListenerPort;
import com.tomasburgaleta.exampleia.domain.port.AudioProcessingException;
import com.tomasburgaleta.exampleia.domain.port.AudioRecordingPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioRecordingServiceTranscriptionTest {
    
    @Mock
    private AudioRecordingPort audioRecordingPort;
    
    @Mock
    private AudioListenerPort audioListenerPort;
    
    @Mock
    private SilenceDetectionService silenceDetectionService;
    
    @Mock
    private AiServicePort aiServicePort;
    
    private AudioRecordingService audioRecordingService;
    
    @BeforeEach
    void setUp() {
        audioRecordingService = new AudioRecordingService(audioRecordingPort, audioListenerPort, silenceDetectionService, aiServicePort);
    }
    
    @Test
    void testTranscribeRecording_Success() throws AudioProcessingException {
        // Arrange
        String recordingId = "test-recording-id";
        byte[] pcmData = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        
        AudioBean storedBean = new AudioBean(recordingId, pcmData);
        storedBean.setSamplesPerSecond(44100);
        storedBean.setBitsPerSample((short) 16);
        storedBean.setChannels((short) 2);
        
        when(audioRecordingPort.getRecording(recordingId)).thenReturn(storedBean);
        
        doAnswer(invocation -> {
            AudioBean bean = invocation.getArgument(0);
            bean.setTranscribedText("Test transcription");
            return bean.getAudioData();
        }).when(audioListenerPort).listenAudio(any(AudioBean.class));
        
        when(aiServicePort.sendPrompt("Test transcription")).thenReturn("AI Response");
        
        // Act
        AudioBean result = audioRecordingService.transcribeRecording(recordingId);
        
        // Assert
        assertNotNull(result);
        assertEquals(recordingId, result.getId());
        assertEquals("Test transcription", result.getTranscribedText());
        assertEquals("AI Response", result.getAiResponse());
        assertEquals(44100L, result.getSamplesPerSecond());
        assertEquals((short) 16, result.getBitsPerSample());
        assertEquals((short) 2, result.getChannels());
        
        verify(audioRecordingPort, times(1)).getRecording(recordingId);
        verify(audioListenerPort, times(1)).listenAudio(any(AudioBean.class));
        verify(aiServicePort, times(1)).sendPrompt("Test transcription");
    }
    
    @Test
    void testTranscribeRecording_RecordingNotFound() throws AudioProcessingException {
        // Arrange
        String recordingId = "non-existent-id";
        when(audioRecordingPort.getRecording(recordingId)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.transcribeRecording(recordingId));
        
        assertTrue(exception.getMessage().contains("Recording not found"));
        assertTrue(exception.getMessage().contains(recordingId));
        
        verify(audioRecordingPort, times(1)).getRecording(recordingId);
        verify(audioListenerPort, never()).listenAudio(any(AudioBean.class));
    }
    
    @Test
    void testTranscribeRecording_NullId() throws AudioProcessingException {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.transcribeRecording(null));
        
        assertTrue(exception.getMessage().contains("Recording ID cannot be null or empty"));
        
        verify(audioRecordingPort, never()).getRecording(anyString());
        verify(audioListenerPort, never()).listenAudio(any(AudioBean.class));
    }
    
    @Test
    void testTranscribeRecording_EmptyId() throws AudioProcessingException {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.transcribeRecording(""));
        
        assertThrows(IllegalArgumentException.class, () -> 
            audioRecordingService.transcribeRecording("   "));
        
        verify(audioRecordingPort, never()).getRecording(anyString());
        verify(audioListenerPort, never()).listenAudio(any(AudioBean.class));
    }
    
    @Test
    void testTranscribeRecording_ProcessingError() throws AudioProcessingException {
        // Arrange
        String recordingId = "test-recording-id";
        byte[] pcmData = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        
        AudioBean storedBean = new AudioBean(recordingId, pcmData);
        storedBean.setSamplesPerSecond(44100);
        storedBean.setBitsPerSample((short) 16);
        storedBean.setChannels((short) 2);
        
        when(audioRecordingPort.getRecording(recordingId)).thenReturn(storedBean);
        when(audioListenerPort.listenAudio(any(AudioBean.class)))
            .thenThrow(new AudioProcessingException("Processing failed"));
        
        // Act & Assert
        assertThrows(AudioProcessingException.class, () -> 
            audioRecordingService.transcribeRecording(recordingId));
        
        verify(audioRecordingPort, times(1)).getRecording(recordingId);
        verify(audioListenerPort, times(1)).listenAudio(any(AudioBean.class));
    }
    
    @Test
    void testTranscribeRecording_WavConversion() throws AudioProcessingException {
        // Arrange
        String recordingId = "test-recording-id";
        byte[] pcmData = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        
        AudioBean storedBean = new AudioBean(recordingId, pcmData);
        storedBean.setSamplesPerSecond(44100);
        storedBean.setBitsPerSample((short) 16);
        storedBean.setChannels((short) 2);
        
        when(audioRecordingPort.getRecording(recordingId)).thenReturn(storedBean);
        
        doAnswer(invocation -> {
            AudioBean bean = invocation.getArgument(0);
            // Verify that the audio data is now in WAV format (has header)
            byte[] wavData = bean.getAudioData();
            
            // Check WAV header
            assertTrue(wavData.length > 44, "WAV data should have header");
            assertEquals('R', (char) wavData[0]);
            assertEquals('I', (char) wavData[1]);
            assertEquals('F', (char) wavData[2]);
            assertEquals('F', (char) wavData[3]);
            
            bean.setTranscribedText("Transcribed from WAV");
            return wavData;
        }).when(audioListenerPort).listenAudio(any(AudioBean.class));
        
        // Act
        AudioBean result = audioRecordingService.transcribeRecording(recordingId);
        
        // Assert
        assertNotNull(result);
        assertEquals("Transcribed from WAV", result.getTranscribedText());
        
        verify(audioListenerPort, times(1)).listenAudio(any(AudioBean.class));
    }
}
