# WAV File Reading Service Documentation

## Overview

The `AudioFileReaderService` provides functionality to read WAV audio files from the file system and convert them to byte arrays. The service includes robust validation to ensure only valid WAV format files are processed.

## Architecture

This service follows Clean Architecture principles:

- **Domain Layer**: `AudioFileReaderPort` interface and `AudioFileException`
- **Application Layer**: `AudioFileReaderService` - orchestrates file reading operations  
- **Infrastructure Layer**: `FileSystemAudioFileReaderAdapter` - handles actual file system operations

## Usage

### Basic Usage

```java
@Service
public class MyAudioService {
    
    private final AudioFileReaderService audioFileReaderService;
    
    public MyAudioService(AudioFileReaderService audioFileReaderService) {
        this.audioFileReaderService = audioFileReaderService;
    }
    
    public void processAudioFile(String filePath) {
        try {
            // Read WAV file from file system
            byte[] audioData = audioFileReaderService.readWavFile(filePath);
            
            // Process the audio data
            System.out.println("Read " + audioData.length + " bytes from WAV file");
            
        } catch (AudioFileException e) {
            // Handle file reading or validation errors
            System.err.println("Failed to read WAV file: " + e.getMessage());
        }
    }
}
```

### Integration with Existing Audio Processing

```java
@Service
public class AudioTranscriptionService {
    
    private final AudioFileReaderService audioFileReaderService;
    private final AudioListenerService audioListenerService;
    
    public AudioTranscriptionService(
            AudioFileReaderService audioFileReaderService,
            AudioListenerService audioListenerService) {
        this.audioFileReaderService = audioFileReaderService;
        this.audioListenerService = audioListenerService;
    }
    
    public String transcribeAudioFile(String filePath) throws AudioProcessingException, AudioFileException {
        // Read WAV file from disk
        byte[] audioData = audioFileReaderService.readWavFile(filePath);
        
        // Create MIObject for processing
        String objectId = UUID.randomUUID().toString();
        MIObject audioBean = new MIObject(objectId, audioData);
        
        // Process with existing audio listener service
        audioListenerService.listenAudio(audioBean);
        
        // Return transcribed text
        return audioBean.getTranscribedText();
    }
}
```

## Validation Features

The service performs comprehensive validation:

### File System Validation
- ✅ File exists
- ✅ File is readable 
- ✅ Path is not a directory
- ✅ File is not empty

### WAV Format Validation
- ✅ Minimum file size (12 bytes for basic header)
- ✅ RIFF header presence (bytes 0-3 must be "RIFF")
- ✅ WAVE format identifier (bytes 8-11 must be "WAVE")

## Error Handling

The service throws `AudioFileException` (a `RuntimeException`) for various error conditions:

```java
try {
    byte[] audioData = audioFileReaderService.readWavFile("/path/to/file.wav");
} catch (AudioFileException e) {
    // Handle specific error cases:
    String message = e.getMessage();
    
    if (message.contains("File not found")) {
        // Handle missing file
    } else if (message.contains("missing RIFF header")) {
        // Handle invalid WAV format
    } else if (message.contains("not readable")) {
        // Handle permission issues
    }
}
```

### Common Error Messages

- `"File not found: {path}"` - File does not exist
- `"File is not readable: {path}"` - Permission or access issues
- `"Path points to a directory, not a file: {path}"` - Path is a directory
- `"File is too small to be a valid WAV file: {path}"` - File smaller than minimum header size
- `"Invalid WAV format: missing RIFF header in file: {path}"` - Not a valid RIFF file
- `"Invalid WAV format: missing WAVE format identifier in file: {path}"` - Not a WAV file

## Spring Configuration

The service is automatically configured as a Spring bean. No manual configuration required:

```java
// Automatically available for dependency injection
@Autowired
private AudioFileReaderService audioFileReaderService;
```

## Supported File Formats

Currently supports:
- ✅ WAV files with RIFF header
- ✅ All WAV variants (8-bit, 16-bit, mono, stereo, various sample rates)

Future extensions could support:
- MP3, FLAC, OGG formats (would require new adapters)

## Performance Considerations

- Files are read entirely into memory as byte arrays
- Suitable for typical audio files (< 100MB)  
- For very large files, consider streaming approaches
- WAV validation is performed on every read (minimal overhead)

## Testing

The service includes comprehensive tests:

- **Unit Tests**: Service and adapter behavior
- **Integration Tests**: End-to-end functionality
- **Error Cases**: All validation scenarios
- **Test Coverage**: 100% of implemented functionality

Run tests with: `mvn test`