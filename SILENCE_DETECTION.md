# Silence Detection Feature

## Overview

The silence detection feature allows the Java backend to analyze audio data and determine if it contains silence. This is useful for automatically detecting when a user has stopped speaking and preventing the processing of silent audio recordings.

## Architecture

The silence detection follows Clean Architecture principles:

### Domain Layer
- **SilenceDetectorPort**: Interface defining the contract for silence detection
  - `boolean detectSilence(byte[] pcmData, long samplesPerSecond, short bitsPerSample, short channels)`

### Application Layer
- **SilenceDetectionService**: Application service that orchestrates silence detection
  - Validates input data
  - Delegates detection to the port implementation
  - Returns boolean indicating if audio is silent

### Infrastructure Layer
- **RmsSilenceDetectorAdapter**: Implementation using RMS (Root Mean Square) amplitude analysis
  - Analyzes audio samples to calculate average amplitude
  - Compares against configurable threshold
  - Supports 8-bit, 16-bit, and 24-bit audio
  - Supports mono and stereo channels

## How It Works

### RMS-Based Detection

The RmsSilenceDetectorAdapter uses the following algorithm:

1. **Sample Reading**: Reads each audio sample and normalizes it to [-1.0, 1.0] range
2. **Amplitude Calculation**: For each sample, calculates the absolute amplitude
3. **Threshold Comparison**: Compares amplitude against `SILENCE_THRESHOLD` (default: 0.01 or 1%)
4. **Percentage Calculation**: Calculates the percentage of silent samples
5. **Decision**: If >= 95% of samples are silent, the audio is considered silent

### Configuration

The adapter has two configurable constants:

```java
// Amplitude threshold for silence (RMS value)
private static final double SILENCE_THRESHOLD = 0.01; // 1% of maximum amplitude

// Minimum percentage of silent samples to consider audio as silent
private static final double SILENT_SAMPLES_PERCENTAGE = 0.95; // 95% must be silent
```

## Integration

### Backend Integration

The silence detection is integrated into the `AudioRecordingController`:

```java
@PostMapping("/start")
public ResponseEntity<Map<String, Object>> startRecording(@RequestBody RecordingRequest request) {
    // Store recording
    AudioBean audioBean = audioRecordingService.startRecording(/* ... */);
    
    // Detect silence
    boolean isSilent = audioRecordingService.detectSilence(
        request.getPcmData(),
        request.getSamplesPerSecond(),
        request.getBitsPerSample(),
        request.getChannels()
    );
    
    // Include in response
    response.put("isSilent", isSilent);
    
    return ResponseEntity.ok(response);
}
```

### Frontend Integration

The JavaScript frontend receives the silence detection result:

```javascript
const saveData = await saveResponse.json();

// Check if silence was detected
if (saveData.isSilent) {
    console.log('Silencio detectado en el audio grabado');
    showError('Se detectó silencio en el audio. Por favor, hable más alto o acérquese al micrófono.');
    return;
}
```

## Supported Audio Formats

The silence detector supports:

- **Sample Rates**: Any sample rate (8kHz, 16kHz, 44.1kHz, 48kHz, etc.)
- **Bit Depths**: 
  - 8-bit (unsigned)
  - 16-bit (signed)
  - 24-bit (signed)
- **Channels**:
  - Mono (1 channel)
  - Stereo (2 channels)

## Use Cases

1. **Automatic Recording Validation**: Prevent submission of silent recordings
2. **User Feedback**: Inform users when their microphone isn't picking up audio
3. **Quality Control**: Ensure audio quality before transcription
4. **Resource Optimization**: Avoid processing silent audio with transcription services

## Testing

The feature includes comprehensive tests:

### Unit Tests
- `SilenceDetectionServiceTest`: Tests the application service
- `RmsSilenceDetectorAdapterTest`: Tests the RMS detection algorithm
  - Tests with null/empty data
  - Tests with different bit depths (8, 16, 24)
  - Tests with mono and stereo audio
  - Tests with varying amplitude levels
  - Tests edge cases (mixed silent/audible audio)

### Integration Tests
The silence detection is tested as part of the `AudioRecordingControllerTest`.

## API Response Format

When storing audio via `/api/recording/start`, the response includes silence detection:

```json
{
  "id": "uuid-of-recording",
  "samplesPerSecond": 44100,
  "bitsPerSample": 16,
  "channels": 2,
  "dataSize": 88200,
  "isSilent": false,
  "success": true
}
```

**Nota**: Si `isSilent` es `true`, el frontend mostrará un mensaje al usuario indicando que se detectó silencio y no procederá con la transcripción.

## Future Enhancements

Potential improvements:

1. **Configurable Thresholds**: Allow users to configure sensitivity
2. **Adaptive Detection**: Adjust thresholds based on background noise
3. **Real-time Streaming**: Detect silence during recording (via WebSocket)
4. **Advanced Algorithms**: Implement Voice Activity Detection (VAD)
5. **Noise Profiling**: Learn background noise patterns for better detection
