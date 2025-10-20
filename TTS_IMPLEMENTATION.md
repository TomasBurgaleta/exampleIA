# Text-to-Speech (TTS) Implementation with ElevenLabs

## Overview

This document describes the implementation of Text-to-Speech (TTS) functionality using ElevenLabs API. The TTS system converts AI-generated text responses to speech audio that is automatically played in the web interface.

## Architecture

Following Clean Architecture principles, the TTS implementation is organized across layers:

### 1. Domain Layer (`domain`)
- **TextToSpeechPort**: Interface defining the contract for TTS services
  - `synthesizeSpeech(String text)`: Converts text to audio bytes
  - `isAvailable()`: Checks if TTS service is configured

### 2. Application Layer (`application`)
- **TextToSpeechService**: Application service orchestrating TTS operations
  - Validates input text
  - Checks service availability
  - Delegates to port implementation

### 3. Infrastructure Layer (`infrastructure`)
- **ElevenLabsAdapter**: Implementation of TextToSpeechPort using ElevenLabs API
  - Makes HTTP requests to ElevenLabs API
  - Handles audio generation and error cases
- **ElevenLabsConfig**: Configuration properties for API key and voice settings

### 4. Web Layer (`web`)
- **TextToSpeechController**: REST endpoint for TTS requests
  - POST `/api/tts/synthesize`: Converts text to speech audio
  - GET `/api/tts/health`: Health check endpoint
- **Frontend Integration**: JavaScript functions for audio playback

## Configuration

Add the following properties to `application.properties`:

```properties
# ElevenLabs Text-to-Speech Configuration
elevenlabs.api-key=${ELEVENLABS_API_KEY:your-elevenlabs-api-key-here}
elevenlabs.voice-id=${ELEVENLABS_VOICE_ID:your-voice-id-here}
elevenlabs.model-id=${ELEVENLABS_MODEL_ID:eleven_multilingual_v2}
```

### Required Configuration:
- **API Key**: Your ElevenLabs API key (get it from https://elevenlabs.io)
- **Voice ID**: The ID of the voice to use for speech synthesis
- **Model ID**: The TTS model (default: eleven_multilingual_v2)

## API Endpoints

### 1. Synthesize Speech
**POST** `/api/tts/synthesize`

**Request Body:**
```json
{
  "text": "Hello, this is a test of text to speech"
}
```

**Response:**
- Content-Type: `audio/mpeg`
- Returns MP3 audio file as binary data

**Error Response:**
```json
{
  "error": "Error message description"
}
```

### 2. Health Check
**GET** `/api/tts/health`

**Response:**
```json
{
  "service": "Text-to-Speech",
  "available": true,
  "status": "UP"
}
```

## Frontend Integration

### Automatic TTS Playback

When an AI response is received, TTS audio is automatically generated and played:

1. JavaScript detects AI response in result data
2. Calls `/api/tts/synthesize` with the AI response text
3. Receives MP3 audio blob
4. Creates Audio element and plays automatically
5. Shows visual status indicators (loading, playing, completed)

### Manual Playback Button

Users can replay the TTS audio using the "🔊 Reproducir Audio" button in the AI response section.

### Status Indicators

The TTS system shows different status messages:
- ⏳ **Generando audio...**: TTS request in progress
- 🔊 **Reproduciendo...**: Audio is playing
- ✅ **Audio completado**: Playback finished
- ❌ **Error**: Error occurred during generation or playback

## Code Examples

### Using TTS in Application Code

```java
@Autowired
private TextToSpeechService textToSpeechService;

public void processAiResponse(String aiResponse) {
    try {
        if (textToSpeechService.isServiceAvailable()) {
            byte[] audioData = textToSpeechService.convertTextToSpeech(aiResponse);
            // Handle audio data (e.g., save to file, stream to client)
        }
    } catch (AudioProcessingException e) {
        // Handle error
    }
}
```

### JavaScript Usage

```javascript
async function playTextToSpeech(text) {
    const response = await fetch('/api/tts/synthesize', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: text })
    });
    
    const audioBlob = await response.blob();
    const audio = new Audio(URL.createObjectURL(audioBlob));
    audio.play();
}
```

## Testing

### Unit Tests

Comprehensive tests are available in `TextToSpeechServiceTest`:
- Text validation (null, empty, blank)
- Service availability checks
- Successful conversion
- Error handling and propagation

Run tests:
```bash
mvn test -Dtest=TextToSpeechServiceTest
```

### Integration Testing

To test the full TTS flow:
1. Configure valid ElevenLabs credentials
2. Start the application
3. Transcribe audio that triggers an AI response
4. Verify TTS audio plays automatically

## Error Handling

The system handles various error scenarios:

1. **Configuration Errors**: Service not available if API key or voice ID missing
2. **API Errors**: HTTP errors from ElevenLabs API are caught and reported
3. **Network Errors**: Connection issues are handled gracefully
4. **Audio Playback Errors**: Frontend handles audio element errors

## Dependencies

### Maven Dependencies

The implementation uses:
- **Spring Web**: RestTemplate for HTTP requests
- **Spring Boot**: Configuration management

Added to `infrastructure/pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

## Performance Considerations

1. **API Latency**: ElevenLabs API typically responds in 1-3 seconds
2. **Audio Format**: MP3 format provides good quality with reasonable file size
3. **Caching**: Consider implementing caching for repeated texts (future enhancement)
4. **Rate Limiting**: Be aware of ElevenLabs API rate limits

## Security Considerations

1. **API Key Protection**: Never commit API keys to source control
2. **Environment Variables**: Use environment variables for sensitive data
3. **Input Validation**: Text length and content are validated
4. **Error Messages**: Generic error messages prevent information leakage

## Future Enhancements

Potential improvements:
1. Audio caching for frequently used responses
2. Multiple voice support with voice selection UI
3. Speech rate and pitch control
4. Support for additional TTS providers
5. Audio format selection (MP3, WAV, OGG)
6. Streaming audio generation for long texts

## Troubleshooting

### TTS Not Working

1. **Check Configuration**:
   - Verify `elevenlabs.api-key` is set
   - Verify `elevenlabs.voice-id` is set
   - Check `/api/tts/health` endpoint

2. **Check Logs**:
   - Look for ElevenLabsAdapter initialization messages
   - Check for API communication errors

3. **Verify Voice ID**:
   - Ensure the voice ID exists in your ElevenLabs account
   - Try with a different voice ID

### Audio Not Playing

1. **Browser Console**: Check for JavaScript errors
2. **Network Tab**: Verify API request succeeds and returns audio
3. **Audio Format**: Ensure browser supports MP3 playback
4. **Browser Autoplay**: Some browsers block autoplay; check settings

## References

- [ElevenLabs API Documentation](https://elevenlabs.io/docs)
- [Clean Architecture Principles](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Web Audio API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Audio_API)
