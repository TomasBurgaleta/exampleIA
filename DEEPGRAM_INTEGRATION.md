# Deepgram Integration for Speech-to-Text

## Overview

This document describes the integration of Deepgram as an alternative speech-to-text provider alongside Microsoft Azure Speech Services. The implementation follows Clean Architecture principles with a common interface that allows switching between providers via configuration.

## Architecture

### Common Interface: SpeechToTextPort

A new domain interface `SpeechToTextPort` has been created in the `domain` layer to define the contract for speech-to-text services:

```java
public interface SpeechToTextPort {
    byte[] transcribe(AudioBean audioBean) throws AudioProcessingException;
}
```

### Implementations

Both Azure and Deepgram adapters implement this common interface:

1. **AzureAudioListenerAdapter** - Uses Microsoft Azure Cognitive Services Speech SDK
2. **DeepgramAudioListenerAdapter** - Uses Deepgram REST API

Both adapters also implement the existing `AudioListenerPort` interface for backward compatibility.

## Configuration

### Provider Selection

The speech-to-text provider is selected via the `speech.provider` configuration property:

```properties
# Speech-to-Text Provider Selection (azure or deepgram)
speech.provider=azure  # or deepgram
```

### Azure Configuration

```properties
azure.speech.subscription-key=${AZURE_SPEECH_KEY:your-subscription-key-here}
azure.speech.region=${AZURE_SPEECH_REGION:westeurope}
azure.speech.language=${AZURE_SPEECH_LANGUAGE:es-ES}
```

### Deepgram Configuration

```properties
deepgram.speech.api-key=${DEEPGRAM_API_KEY:your-deepgram-api-key-here}
deepgram.speech.model=${DEEPGRAM_MODEL:nova-2}
deepgram.speech.language=${DEEPGRAM_LANGUAGE:es}
```

## Using Deepgram

### Prerequisites

1. Obtain a Deepgram API key from [https://deepgram.com](https://deepgram.com)
2. Set the API key as an environment variable or in `application.properties`

### Switching to Deepgram

To use Deepgram instead of Azure:

1. Set your Deepgram API key:
   ```bash
   export DEEPGRAM_API_KEY=your-api-key-here
   ```

2. Configure the provider:
   ```bash
   export SPEECH_PROVIDER=deepgram
   ```

3. (Optional) Configure the model and language:
   ```bash
   export DEEPGRAM_MODEL=nova-2
   export DEEPGRAM_LANGUAGE=es
   ```

### Supported Models

Deepgram supports several models:
- `nova-2` (default) - Latest and most accurate model
- `nova` - Previous generation model
- `enhanced` - Enhanced model for specific use cases
- `base` - Base model for general use

### Language Support

Deepgram supports language detection and transcription in multiple languages. The `language` parameter specifies the default language but automatic detection is enabled.

Common language codes:
- `es` - Spanish
- `en` - English
- `fr` - French
- `de` - German
- `it` - Italian
- `pt` - Portuguese

## Features

Both providers support:
- Speech-to-text transcription
- Automatic language detection
- Multiple audio formats (WAV/PCM)
- Configurable sample rates and bit depths

### Deepgram-Specific Features

- REST API-based integration (no SDK dependency required)
- Automatic punctuation
- Language detection enabled by default
- Support for Linear16 PCM encoding

## Implementation Details

### DeepgramAudioListenerAdapter

The adapter:
1. Validates audio metadata (sample rate, bit depth, channels)
2. Constructs REST API request with appropriate parameters
3. Sends audio data as binary payload
4. Parses the JSON response to extract transcription and detected language
5. Updates the `AudioBean` with results

### Error Handling

The adapter handles various error scenarios:
- Invalid configuration (missing API key)
- Empty or null audio data
- Invalid audio metadata
- API communication errors
- Response parsing errors

All errors are wrapped in `AudioProcessingException` for consistent error handling across the application.

## Testing

Comprehensive unit tests are provided in `DeepgramAudioListenerAdapterTest` covering:
- Successful transcription
- Configuration validation
- Error handling
- Empty responses
- API failures
- Metadata validation

Run tests with:
```bash
mvn test
```

## Clean Architecture Compliance

The implementation follows Clean Architecture principles:

1. **Domain Layer**: `SpeechToTextPort` interface defines the contract
2. **Application Layer**: Services use the port without knowing the implementation
3. **Infrastructure Layer**: Adapters implement the port using external services
4. **Configuration**: Bean creation in `InfrastructureConfig` controls which implementation is used

## Benefits

1. **Flexibility**: Easy switching between providers
2. **Testability**: Both providers can be mocked and tested independently
3. **Maintainability**: Common interface ensures consistent behavior
4. **Scalability**: New providers can be added by implementing `SpeechToTextPort`
5. **Clean Architecture**: Clear separation of concerns and dependency inversion

## Future Enhancements

Potential improvements:
- Support for additional Deepgram features (diarization, custom vocabulary)
- Streaming transcription support
- Additional speech-to-text providers (Google Cloud, AWS)
- Fallback mechanism when primary provider fails
- Provider-specific configuration profiles

## Troubleshooting

### Common Issues

1. **"Deepgram configuration is invalid"**
   - Ensure `DEEPGRAM_API_KEY` is set correctly
   - Check that the API key is valid

2. **"Deepgram API call failed"**
   - Verify network connectivity
   - Check API key permissions
   - Review Deepgram API status

3. **Empty transcriptions**
   - Ensure audio quality is sufficient
   - Verify audio format is correct (Linear16 PCM)
   - Check sample rate matches audio data

### Debug Logging

Enable debug logging for troubleshooting:
```properties
logging.level.com.tomasburgaleta.exampleia.infrastructure.adapter=DEBUG
```

## References

- [Deepgram API Documentation](https://developers.deepgram.com/docs)
- [Deepgram Speech-to-Text API](https://developers.deepgram.com/reference/pre-recorded)
- [Clean Architecture Principles](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
