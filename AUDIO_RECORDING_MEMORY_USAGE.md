# Audio Recording Memory Storage - Usage Guide

## Overview

This feature allows you to record audio in the web browser and save it directly in the server's memory as PCM (Pulse Code Modulation) data along with its metadata. This follows the hexagonal architecture pattern used throughout the project.

## Architecture

The implementation follows Clean Architecture/Hexagonal Architecture principles:

### Domain Layer
- **AudioRecordingPort**: Interface that defines the contract for audio recording operations
  - `storeRecording(AudioBean)`: Stores audio in memory
  - `getRecording(String id)`: Retrieves stored recording
  - `clearRecording(String id)`: Removes recording from memory

### Application Layer
- **AudioRecordingService**: Orchestrates the business logic
  - Validates input parameters
  - Generates unique IDs for recordings
  - Delegates storage to the port implementation

### Infrastructure Layer
- **InMemoryAudioRecordingAdapter**: Concrete implementation using ConcurrentHashMap
  - Thread-safe storage
  - In-memory persistence (data lost on restart)

### Web Layer
- **AudioRecordingController**: REST API endpoints
  - `POST /api/recording/start`: Save PCM audio with metadata
  - `GET /api/recording/{id}`: Retrieve recording info
  - `DELETE /api/recording/{id}`: Clear recording

## Using the Web Interface

1. **Configure Recording Settings**:
   - Select sample rate (8 kHz, 16 kHz, or 44.1 kHz)
   - Select bit depth (8, 16, or 24 bits)

2. **Record Audio**:
   - Click "🎤 Iniciar Grabación"
   - Speak into your microphone
   - Click "⏹️ Detener" when done

3. **Save to Memory**:
   - Click "💾 Guardar en Memoria"
   - The system extracts PCM data from the WAV file
   - Sends to the server with metadata

4. **View Stored Recording**:
   - See the recording ID
   - View PCM data size
   - See audio metadata (sample rate, bit depth, channels)

5. **Clear from Memory**:
   - Click "🗑️ Limpiar de Memoria" to remove
   - Or click "🔄 Nueva Grabación" to start over

## API Usage Examples

### Store a Recording

```bash
curl -X POST http://localhost:8080/api/recording/start \
  -H "Content-Type: application/json" \
  -d '{
    "pcmData": [1, 2, 3, 4, 5, ...],
    "samplesPerSecond": 44100,
    "bitsPerSample": 16,
    "channels": 2
  }'
```

Response:
```json
{
  "success": true,
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "samplesPerSecond": 44100,
  "bitsPerSample": 16,
  "channels": 2,
  "dataSize": 1024000
}
```

### Retrieve a Recording

```bash
curl http://localhost:8080/api/recording/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

Response:
```json
{
  "success": true,
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "samplesPerSecond": 44100,
  "bitsPerSample": 16,
  "channels": 2,
  "dataSize": 1024000
}
```

### Clear a Recording

```bash
curl -X DELETE http://localhost:8080/api/recording/f47ac10b-58cc-4372-a567-0e02b2c3d479
```

Response:
```json
{
  "success": true,
  "message": "Recording stopped and cleared"
}
```

## Data Format

The `AudioBean` object contains:

- **id** (String): Unique identifier (UUID)
- **audioData** (byte[]): Raw PCM audio data
- **samplesPerSecond** (long): Sample rate in Hz (e.g., 44100)
- **bitsPerSample** (short): Bit depth (e.g., 16)
- **channels** (short): Number of channels (1 = mono, 2 = stereo)

## Notes

- Audio is stored in **memory only** - data is lost on server restart
- PCM data is extracted from WAV files (header removed)
- Storage uses `ConcurrentHashMap` for thread safety
- Each recording gets a unique UUID identifier
- No size limits enforced (be mindful of memory usage)

## Testing

The implementation includes comprehensive unit tests:

- **AudioRecordingServiceTest**: 17 test cases
- **InMemoryAudioRecordingAdapterTest**: 12 test cases

Run tests with:
```bash
mvn test
```

## Future Enhancements

Possible improvements:
- Persistent storage (database or file system)
- Size limits and cleanup policies
- Audio format conversion
- Compression support
- Streaming capabilities
