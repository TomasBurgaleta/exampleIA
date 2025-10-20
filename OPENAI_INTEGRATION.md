# OpenAI Integration Documentation

## Overview

This project now integrates OpenAI using LangChain4j library to provide AI-powered responses based on transcribed audio text. When audio is transcribed using Azure Speech Services, the transcribed text is automatically sent to OpenAI, and the AI's response is displayed alongside the transcription.

## Architecture

The implementation follows Clean Architecture principles with clear separation of concerns:

### Domain Layer
- **AiServicePort**: Port interface defining the contract for AI service integration
  - Located: `domain/src/main/java/.../domain/port/AiServicePort.java`
  - Method: `String sendPrompt(String prompt)`

### Infrastructure Layer
- **OpenAiConfig**: Configuration class for OpenAI settings
  - Located: `infrastructure/src/main/java/.../infrastructure/config/OpenAiConfig.java`
  - Manages: API key, model selection, temperature, max tokens, logging preferences

- **OpenAiAdapter**: Adapter implementing AiServicePort using LangChain4j
  - Located: `infrastructure/src/main/java/.../infrastructure/adapter/OpenAiAdapter.java`
  - Uses: LangChain4j's OpenAiChatModel for communication with OpenAI API

### Application Layer
- **AudioRecordingService**: Updated to integrate AI responses
  - Sends transcribed text to AI service
  - Stores AI response in AudioBean

### Web Layer
- **AudioRecordingController**: Updated to include AI response in API responses
- **Web Interface**: Updated to display AI responses with distinct styling

## Configuration

Add the following properties to `application.properties`:

```properties
# OpenAI Configuration
openai.api-key=${OPENAI_API_KEY:your-openai-api-key-here}
openai.model=${OPENAI_MODEL:gpt-3.5-turbo}
openai.temperature=${OPENAI_TEMPERATURE:0.7}
openai.max-tokens=${OPENAI_MAX_TOKENS:150}
openai.log-requests=${OPENAI_LOG_REQUESTS:true}
openai.log-responses=${OPENAI_LOG_RESPONSES:true}
```

### Environment Variables

Set your OpenAI API key:

```bash
export OPENAI_API_KEY=your-actual-api-key
```

## Supported Models

The integration uses `gpt-3.5-turbo` by default, but you can configure other OpenAI models:
- gpt-3.5-turbo
- gpt-4
- gpt-4-turbo-preview
- And other OpenAI chat models

## Usage Flow

1. **Audio Recording**: User records audio through the web interface
2. **Transcription**: Audio is sent to Azure Speech Services for transcription
3. **AI Processing**: Transcribed text is automatically sent to OpenAI
4. **Display Results**: Both transcription and AI response are displayed to the user

## API Response Format

When transcribing audio, the API now returns:

```json
{
  "id": "recording-id",
  "transcribedText": "Text detected from audio",
  "aiResponse": "AI's response to the transcribed text",
  "hasTranscription": true,
  "hasAiResponse": true,
  "audioSize": 12345,
  "samplesPerSecond": 44100,
  "bitsPerSample": 16,
  "channels": 2,
  "success": true
}
```

## Extensibility

The design uses the **Strategy Pattern** through the AiServicePort interface, making it easy to:

1. **Switch AI Providers**: Create a new adapter implementing AiServicePort
   - Example: `ClaudeAdapter`, `GeminiAdapter`, `HuggingFaceAdapter`
   
2. **Multiple AI Services**: Configure multiple AI adapters and select based on use case

3. **Custom Logic**: Add preprocessing or postprocessing of prompts and responses

### Example: Adding a New AI Provider

```java
@Component
public class ClaudeAdapter implements AiServicePort {
    
    private final ClaudeConfig claudeConfig;
    
    public ClaudeAdapter(ClaudeConfig claudeConfig) {
        this.claudeConfig = claudeConfig;
    }
    
    @Override
    public String sendPrompt(String prompt) throws AudioProcessingException {
        // Implement Claude API integration
        return claudeClient.generate(prompt);
    }
}
```

Then update `InfrastructureConfig` to use the new adapter:

```java
@Bean
public AiServicePort aiServicePort(ClaudeConfig claudeConfig) {
    return new ClaudeAdapter(claudeConfig);
}
```

## Error Handling

- If OpenAI API key is missing or invalid, the system logs a warning but continues with transcription
- If AI processing fails, the transcription result is still returned
- All errors are logged with detailed messages for debugging

## Testing

Unit tests are provided for:
- `OpenAiAdapter`: Tests configuration, validation, and error handling
- `AudioRecordingService`: Tests AI integration in transcription flow
- All tests use Mockito for dependency injection

Run tests:
```bash
mvn test
```

## Security Considerations

- API keys are configured via environment variables (never hardcoded)
- Logging of requests/responses can be disabled in production
- No sensitive data is stored in the codebase
- CodeQL security scanning passes with no alerts

## Dependencies

Added to parent POM:
```xml
<langchain4j.version>0.34.0</langchain4j.version>

<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
    <version>${langchain4j.version}</version>
</dependency>
```

## Future Enhancements

Potential improvements:
1. **Streaming Responses**: Use OpenAI streaming API for real-time AI responses
2. **Context Management**: Maintain conversation history for multi-turn interactions
3. **Prompt Templates**: Configure different prompts for different use cases
4. **Model Selection**: Allow users to select AI model via UI
5. **Response Caching**: Cache AI responses for identical transcriptions
6. **Rate Limiting**: Implement rate limiting to manage API costs
7. **Multiple Languages**: Language-specific prompts based on Azure language detection
