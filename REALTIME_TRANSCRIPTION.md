# Real-Time Audio Transcription Feature

## Overview

This feature provides real-time audio transcription by streaming audio chunks from the browser to the backend, which then sends them to Azure Speech Services for transcription.

## Architecture

### Backend Components

#### 1. AudioStreamingService (Application Layer)
Located at: `application/src/main/java/com/tomasburgaleta/exampleia/application/service/AudioStreamingService.java`

**Purpose**: Manages thread-safe accumulation of audio bytes.

**Key Methods**:
- `addAudioBytes(byte[] audioChunk)`: Adds audio bytes to the buffer in a synchronized manner
- `extractAndClearAudioBytes()`: Extracts all accumulated bytes and clears the buffer (synchronized)
- `getBufferSize()`: Returns the current buffer size
- `isEmpty()`: Checks if the buffer is empty
- `clear()`: Clears the buffer

**Thread Safety**: Uses `synchronized` keyword and an internal lock object to ensure that add and extract operations are mutually exclusive and never happen concurrently.

#### 2. AudioStreamingController (Web Layer)
Located at: `web/src/main/java/com/tomasburgaleta/exampleia/web/controller/AudioStreamingController.java`

**Purpose**: Provides REST endpoints for audio streaming.

**Endpoints**:

##### POST /api/stream/start
Starts a new streaming session.

**Request Body**:
```json
{
  "samplesPerSecond": 16000,
  "bitsPerSample": 16,
  "channels": 1
}
```

**Response**:
```json
{
  "sessionId": "uuid-string",
  "success": true
}
```

##### POST /api/stream/chunk
Sends an audio chunk during recording.

**Request Body**:
```json
{
  "sessionId": "uuid-string",
  "pcmData": [1, 2, 3, ...]
}
```

**Response**:
```json
{
  "success": true,
  "bufferSize": 1024,
  "isSilent": false
}
```

##### POST /api/stream/stop
Stops the session and gets the transcription.

**Request Body**:
```json
{
  "sessionId": "uuid-string"
}
```

**Response**:
```json
{
  "success": true,
  "sessionId": "uuid-string",
  "transcribedText": "Transcribed text here",
  "hasTranscription": true,
  "audioSize": 10240
}
```

##### GET /api/stream/transcription/{sessionId}
Gets the current transcription status.

**Response**:
```json
{
  "sessionId": "uuid-string",
  "transcribedText": "Transcribed text here",
  "hasTranscription": true,
  "success": true
}
```

### Frontend Components

#### 1. Real-Time Transcription UI
Located at: `web/src/main/resources/templates/index.html`

**Elements**:
- `#realtimeTranscriptionArea`: Container for the transcription display (initially hidden)
- `#realtimeTranscriptionText`: Textarea showing the transcription
- `#transcriptionStatus`: Status indicator showing current processing state

#### 2. Streaming Logic
Located at: `web/src/main/resources/static/js/app.js`

**Key Variables**:
- `streamingSessionId`: Current session ID
- `silenceThreshold`: 1000ms (1 second) of silence before auto-stop
- `lastAudioTime`: Timestamp of last detected audio

**Key Functions**:

##### startRecording()
1. Shows the real-time transcription area
2. Starts a streaming session via `/api/stream/start`
3. Configures MediaRecorder to send chunks every 250ms
4. Starts silence detection interval

##### stopRecording()
1. Stops the MediaRecorder
2. Calls `/api/stream/stop` to get final transcription
3. Updates the transcription textarea
4. Shows completion status

##### checkForSilence()
1. Analyzes current audio volume
2. Tracks time since last detected sound
3. Auto-stops recording after 1 second of silence

##### mediaRecorder.ondataavailable
1. Converts each audio chunk to PCM format
2. Sends chunk to `/api/stream/chunk`
3. Updates buffer size and silence status display

## Workflow

### Recording Flow

1. **User clicks "Iniciar Grabación"**
   - Frontend calls `startRecording()`
   - Session is created via `/api/stream/start`
   - Real-time transcription area becomes visible
   - MediaRecorder starts with 250ms chunk interval

2. **During Recording**
   - Every 250ms, MediaRecorder fires `ondataavailable`
   - Audio chunk is converted to PCM
   - Chunk is sent to `/api/stream/chunk`
   - Backend adds chunk to buffer via `AudioStreamingService.addAudioBytes()`
   - Backend checks for silence
   - Frontend updates status display

3. **Silence Detection**
   - `checkForSilence()` runs every 100ms
   - Monitors volume level from analyser
   - If volume < 10 for 1 second, auto-stops recording

4. **User clicks "Detener" OR 1 second silence detected**
   - MediaRecorder stops
   - Frontend calls `/api/stream/stop`
   - Backend extracts all buffered audio via `AudioStreamingService.extractAndClearAudioBytes()`
   - Backend converts PCM to WAV format
   - Backend sends WAV to Azure Speech Services
   - Azure returns transcription
   - Frontend displays transcription in textarea
   - Session is cleaned up

## Thread Safety

The `AudioStreamingService` ensures thread safety through:

1. **Synchronized Methods**: All public methods are synchronized
2. **Internal Lock**: Uses a lock object for fine-grained synchronization
3. **Atomic Operations**: Add and extract operations are atomic and mutually exclusive
4. **ByteArrayOutputStream**: Thread-safe internal buffer management

This prevents race conditions when:
- Multiple chunks arrive simultaneously
- A stop request comes while a chunk is being processed
- Multiple clients try to access the same session (though only one session is active at a time in current implementation)

## Audio Format Handling

### Browser → Backend
- Format: Raw PCM data
- Encoding: Little-endian
- Chunk size: ~250ms of audio

### Backend → Azure
- Format: WAV with header
- Encoding: PCM
- Header: 44-byte WAV header with proper metadata

### Conversion Process
```
Browser Audio Chunk (WebM/Opus)
    ↓
AudioContext.decodeAudioData()
    ↓
Extract PCM samples
    ↓
Send to backend
    ↓
Accumulate in AudioStreamingService
    ↓
Extract all on stop
    ↓
Add WAV header
    ↓
Send to Azure Speech Services
    ↓
Return transcription
```

## Error Handling

### Frontend
- Invalid session: Shows error message
- Empty audio: Shows warning
- Network errors: Displays error status
- Azure errors: Shows error in status area

### Backend
- Null/empty chunks: Returns 400 Bad Request
- Invalid session: Returns 400 Bad Request
- No audio data on stop: Returns 400 Bad Request
- Azure errors: Returns 500 with error message
- Thread synchronization: Handled by synchronized methods

## Testing

### Unit Tests
- `AudioStreamingServiceTest`: Tests thread-safe operations
- `AudioStreamingControllerTest`: Tests REST endpoints

### Integration Tests
Run all tests with:
```bash
mvn test
```

## Configuration

### Silence Detection
Adjust in `app.js`:
```javascript
let silenceThreshold = 1000; // 1 second in milliseconds
```

### Chunk Interval
Adjust in `startRecording()`:
```javascript
mediaRecorder.start(250); // Send chunks every 250ms
```

### Audio Quality
Users can select in the UI:
- Sample Rate: 8 kHz, 16 kHz, 44.1 kHz
- Bit Depth: 8, 16, 24 bits
- Channels: 1 (Mono), 2 (Stereo)

## Future Enhancements

Potential improvements:
1. Support for multiple concurrent sessions
2. Intermediate transcription updates during recording
3. Configurable silence threshold in UI
4. Audio visualization during recording
5. Retry mechanism for failed chunks
6. Buffering strategies for poor network conditions
7. WebSocket support for true real-time updates
