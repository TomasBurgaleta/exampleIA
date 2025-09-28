# WAV Byte Reader Usage Example

This document demonstrates how to use the new WAV byte reader functionality that processes WAV audio data from byte arrays.

## Overview

The new functionality includes:
- **WavByteReaderPort**: Domain interface for processing WAV byte arrays
- **WavByteReaderAdapter**: Infrastructure implementation that validates WAV format and extracts metadata
- **WavByteProcessingService**: Application service that orchestrates the processing
- **AudioBean**: Enhanced domain entity that now contains WAV metadata

## Key Features

- Validates WAV format (RIFF header and WAVE format)
- Extracts WAV metadata: `samplesPerSecond`, `bitsPerSample`, `channels`
- Returns PCM audio data **without the WAV header**
- Supports various WAV formats (mono/stereo, different sample rates, bit depths)
- Only supports PCM format (non-compressed audio)

## Usage Example

```java
import com.tomasburgaleta.exampleia.application.service.WavByteProcessingService;
import com.tomasburgaleta.exampleia.domain.model.AudioBean;
import com.tomasburgaleta.exampleia.infrastructure.adapter.WavByteReaderAdapter;

public class WavProcessorExample {
    
    public static void main(String[] args) {
        // Set up the service (in real application, use dependency injection)
        WavByteReaderAdapter adapter = new WavByteReaderAdapter();
        WavByteProcessingService service = new WavByteProcessingService(adapter);
        
        try {
            // Your WAV file as byte array (from file, network, etc.)
            byte[] wavFileBytes = loadWavFileBytes(); // Your implementation
            
            // Process the WAV data
            AudioBean audioBean = service.processWavBytes(wavFileBytes, "unique-audio-id");
            
            // Access extracted information
            System.out.println("Audio ID: " + audioBean.getId());
            System.out.println("Sample Rate: " + audioBean.getSamplesPerSecond() + " Hz");
            System.out.println("Bit Depth: " + audioBean.getBitsPerSample() + " bits");
            System.out.println("Channels: " + audioBean.getChannels());
            System.out.println("PCM Data Size: " + audioBean.getAudioData().length + " bytes");
            
            // The audioBean.getAudioData() contains only the PCM data without WAV header
            byte[] pcmAudioData = audioBean.getAudioData();
            
        } catch (AudioFileException e) {
            System.err.println("Invalid WAV format: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid parameters: " + e.getMessage());
        }
    }
    
    private static byte[] loadWavFileBytes() {
        // Implementation depends on your source (file, network, database, etc.)
        // This is just a placeholder
        return new byte[0];
    }
}
```

## Spring Boot Integration Example

If you're using Spring Boot, you can inject the service:

```java
@RestController
@RequestMapping("/audio")
public class AudioController {
    
    private final WavByteProcessingService wavByteProcessingService;
    
    public AudioController(WavByteProcessingService wavByteProcessingService) {
        this.wavByteProcessingService = wavByteProcessingService;
    }
    
    @PostMapping("/process")
    public ResponseEntity<AudioInfoResponse> processWavFile(
            @RequestParam("file") MultipartFile file) {
        
        try {
            byte[] wavBytes = file.getBytes();
            AudioBean audioBean = wavByteProcessingService.processWavBytes(
                wavBytes, 
                UUID.randomUUID().toString()
            );
            
            AudioInfoResponse response = new AudioInfoResponse(
                audioBean.getId(),
                audioBean.getSamplesPerSecond(),
                audioBean.getBitsPerSample(),
                audioBean.getChannels(),
                audioBean.getAudioData().length
            );
            
            return ResponseEntity.ok(response);
            
        } catch (AudioFileException e) {
            return ResponseEntity.badRequest()
                .body(new AudioInfoResponse("Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new AudioInfoResponse("Error processing file"));
        }
    }
}
```

## Error Handling

The service throws the following exceptions:

- **AudioFileException**: When the byte array is not a valid WAV file
  - Missing RIFF header
  - Missing WAVE format identifier
  - Missing fmt or data chunks
  - Non-PCM audio format
  - Invalid chunk sizes

- **IllegalArgumentException**: For invalid parameters
  - Null or empty byte array
  - Null or empty ID

## Supported WAV Formats

- **Audio Format**: PCM only (no compressed formats)
- **Channels**: Mono (1) and Stereo (2) 
- **Sample Rates**: Common rates like 8kHz, 16kHz, 22.05kHz, 44.1kHz, 48kHz, etc.
- **Bit Depths**: 8-bit, 16-bit, 24-bit, 32-bit

## Architecture Notes

Following Clean Architecture principles:
- **Domain Layer**: `WavByteReaderPort` interface and `AudioBean` entity
- **Application Layer**: `WavByteProcessingService` orchestrates business logic
- **Infrastructure Layer**: `WavByteReaderAdapter` implements the technical details

This separation ensures testability and allows for easy replacement of the WAV processing implementation if needed.