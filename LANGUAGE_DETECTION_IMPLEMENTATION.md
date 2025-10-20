# Language Detection Implementation

## Overview
This document describes the implementation of automatic language detection with Spanish (Spain) as the default language for the Azure Speech Services integration.

## Requirements Implemented
1. **Azure picks Spanish (Spain) as default language** - Configured in `application.properties` with `azure.speech.language=es-ES`
2. **Auto-detect language if not Spanish** - Uses Azure's AutoDetectSourceLanguageConfig to detect from multiple languages
3. **Display detected language in transcription** - Shows the detected language in the web UI and API responses

## Changes Made

### 1. Domain Model (AudioBean.java)
- Added `detectedLanguage` field to store the language detected by Azure
- Added getter and setter methods for the detected language

### 2. Infrastructure Layer (AzureAudioListenerAdapter.java)
- Implemented Azure Auto-Detection using `AutoDetectSourceLanguageConfig`
- Configured to detect from these languages (Spanish variants prioritized):
  - es-ES (Español - España) - **DEFAULT**
  - es-MX (Español - México)
  - es-AR (Español - Argentina)
  - en-US (English - United States)
  - en-GB (English - United Kingdom)
  - fr-FR (Français - France)
  - de-DE (Deutsch - Deutschland)
  - it-IT (Italiano - Italia)
  - pt-PT (Português - Portugal)
  - pt-BR (Português - Brasil)

- Extracts detected language from Azure result using `PropertyId.SpeechServiceConnection_AutoDetectSourceLanguageResult`
- Falls back to configured default language (es-ES) if auto-detection doesn't return a result

### 3. Web Controllers
- **AudioController.java**: Added `detectedLanguage` to transcription response
- **AudioRecordingController.java**: Added `detectedLanguage` to transcription response

### 4. Web Interface
- **index.html**: Added "🌍 Idioma detectado" field to display the detected language
- **app.js**: 
  - Added `formatLanguage()` function to convert language codes to readable names
  - Updated `showResult()` function to display the detected language
  - Language names are shown in Spanish for better user experience

## How It Works

1. When audio is submitted for transcription, Azure attempts to auto-detect the language
2. The system checks multiple language options with Spanish (Spain) having priority
3. If Azure successfully detects the language, it's stored in the AudioBean
4. If Azure cannot detect the language or returns null, the system defaults to "es-ES"
5. The detected language is included in the API response and displayed in the UI

## Language Display Format

The detected language is displayed in a user-friendly format:
- `es-ES` → "Español (España)"
- `es-MX` → "Español (México)"
- `en-US` → "Inglés (Estados Unidos)"
- etc.

## Testing

All existing tests pass successfully. The implementation:
- ✅ Maintains backward compatibility
- ✅ Does not break existing functionality
- ✅ Follows Clean Architecture principles
- ✅ Properly handles edge cases (null/empty detection results)

## Configuration

The default language can be configured in `application.properties`:
```properties
azure.speech.language=${AZURE_SPEECH_LANGUAGE:es-ES}
```

This allows users to change the default language via environment variable if needed, while maintaining Spanish (Spain) as the system default.
