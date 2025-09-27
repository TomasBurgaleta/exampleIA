# Web Interface Documentation

## Overview

The ExampleIA application now includes a modern web interface built with **Thymeleaf**, **Spring Boot**, and **responsive CSS** that allows users to upload WAV audio files and receive transcriptions using Azure Speech Services.

## Features

### User Interface
- **Modern Design**: Gradient background with card-based layout
- **Responsive**: Works on desktop, tablet, and mobile devices
- **Spanish Language**: User-friendly Spanish interface as requested
- **Real-time Feedback**: Loading states, progress indicators, and error handling
- **File Validation**: Client-side and server-side validation for WAV files

### Functionality
- **File Upload**: Drag-and-drop style file selection for WAV files
- **Size Validation**: Maximum file size of 10MB
- **Format Validation**: Only accepts WAV audio files
- **Azure Integration**: Uses existing REST API for transcription
- **Error Handling**: Comprehensive error messages and recovery options

## Technical Implementation

### Architecture
The web interface follows the Clean Architecture principles established in the project:

- **Web Layer**: `WebController.java` serves Thymeleaf templates
- **Integration**: Uses existing REST API at `/api/audio/transcribe`
- **Frontend**: Pure JavaScript (no frameworks) with modern CSS3

### Files Structure
```
web/src/main/resources/
├── templates/
│   └── index.html              # Main Thymeleaf template
├── static/
│   ├── css/
│   │   └── style.css          # Responsive CSS styles
│   └── js/
│       └── app.js             # Frontend JavaScript logic
└── application.properties      # Configuration
```

## Usage

### Accessing the Web Interface
1. Start the application: `mvn spring-boot:run -pl web`
2. Open browser to: `http://localhost:8080`
3. The main interface will load automatically

### Using the Interface
1. **Select File**: Click "Seleccionar archivo WAV" to choose a WAV file
2. **Validate**: The interface validates file type and size
3. **Upload**: Click "TRANSCRIBIR AUDIO" to process the file
4. **Wait**: Loading indicator shows processing status
5. **Result**: View transcription result or error message

### API Integration
The web interface integrates seamlessly with the existing REST API:
- **Endpoint**: `POST /api/audio/transcribe`
- **Content-Type**: `multipart/form-data`
- **Response**: JSON with transcription result

## Testing

### Cucumber BDD Tests
Comprehensive Cucumber tests verify:
- ✅ Web interface accessibility
- ✅ File upload functionality
- ✅ Error handling for invalid files
- ✅ Error handling for empty files
- ✅ Health check endpoint
- ✅ Integration with backend services

### Running Tests
```bash
# Run all tests
mvn test

# Run only Cucumber tests
mvn test -Dtest=CucumberTestRunner -pl web
```

## Configuration

### Spring Boot Configuration
```properties
# File Upload Limits
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Thymeleaf (auto-configured)
# Templates location: classpath:/templates/
# Static resources: classpath:/static/
```

### Azure Configuration
The web interface uses the same Azure Speech Services configuration:
```properties
azure.speech.subscription-key=${AZURE_SPEECH_KEY}
azure.speech.region=${AZURE_SPEECH_REGION:westeurope}
azure.speech.language=${AZURE_SPEECH_LANGUAGE:es-ES}
```

## Error Handling

### Client-side Validation
- File type validation (only WAV files)
- File size validation (max 10MB)
- Empty file detection
- Real-time user feedback

### Server-side Validation
- Content-Type verification
- File size limits enforced by Spring Boot
- Azure Speech Services error handling
- Comprehensive error messages

## Browser Support

### Supported Browsers
- ✅ Chrome 80+
- ✅ Firefox 75+
- ✅ Safari 13+
- ✅ Edge 80+

### Features Used
- CSS Grid and Flexbox for layout
- CSS Custom Properties (variables)
- Modern JavaScript (ES6+)
- Fetch API for HTTP requests
- File API for file handling

## Deployment Notes

### Production Considerations
1. **HTTPS**: Use HTTPS in production for file uploads
2. **File Storage**: Consider temporary file cleanup
3. **Rate Limiting**: Implement rate limiting for API endpoints
4. **Monitoring**: Monitor Azure Speech Services usage and costs
5. **Error Logging**: Implement comprehensive error logging

### Environment Variables
```bash
export AZURE_SPEECH_KEY=your-azure-speech-key
export AZURE_SPEECH_REGION=westeurope
export AZURE_SPEECH_LANGUAGE=es-ES
```

## Development

### Adding New Features
1. Follow Clean Architecture principles
2. Update Cucumber tests for new functionality  
3. Maintain responsive design patterns
4. Keep Spanish language consistency

### Customization
- **Styling**: Modify `/static/css/style.css`
- **Behavior**: Update `/static/js/app.js`
- **Templates**: Edit `/templates/index.html`
- **Configuration**: Adjust `application.properties`