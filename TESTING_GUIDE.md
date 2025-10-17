# Testing the Real-Time Audio Transcription Feature

## Prerequisites

Before testing, ensure you have:
1. Java 17 or higher installed
2. Maven installed
3. Azure Speech Services credentials configured in `application.properties`
4. A microphone connected to your computer
5. A modern web browser (Chrome, Firefox, Edge, or Safari)

## Azure Configuration

Set up your Azure credentials in `web/src/main/resources/application.properties`:

```properties
azure.speech.subscription-key=YOUR_AZURE_KEY
azure.speech.region=YOUR_AZURE_REGION
azure.speech.language=es-ES
```

## Building the Application

```bash
cd /home/runner/work/exampleIA/exampleIA
mvn clean package
```

## Running the Application

```bash
cd web
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## Testing the Feature

### 1. Access the Application
Open your browser and navigate to: `http://localhost:8080`

### 2. Grant Microphone Permissions
When prompted, allow the browser to access your microphone.

### 3. Configure Audio Settings (Optional)
You can adjust:
- **Frecuencia de muestreo**: 8 kHz, 16 kHz, or 44.1 kHz
- **Profundidad de bits**: 8, 16, or 24 bits
- **Canales**: 1 (Mono) or 2 (Estéreo)

For best results with Azure Speech Services, use:
- 16 kHz sample rate
- 16 bits per sample
- 1 channel (Mono)

### 4. Start Recording
Click the **"🎤 Iniciar Grabación"** button.

**What to expect:**
- Button changes to "🔴 Grabando..."
- Real-time transcription area appears below the recording controls
- Recording timer starts counting
- Volume indicator shows audio levels
- Transcription status shows "Esperando audio..." or "Grabando..."

### 5. Speak into the Microphone
Start speaking in Spanish (or the language configured in Azure).

**What happens:**
- Audio chunks are sent to the backend every 250ms
- Buffer size is displayed in the status
- Silence detection is active (checking every 100ms)

### 6. Stop Recording

You can stop recording in two ways:

#### Option A: Manual Stop
Click the **"⏹️ Detener"** button.

#### Option B: Automatic Stop (Silence Detection)
Stop speaking for 1 second. The system will automatically:
- Detect silence
- Show "Silencio detectado, finalizando..." in the status
- Stop recording automatically

**What happens when stopping:**
- Status changes to "Procesando transcripción..."
- All accumulated audio is sent to Azure Speech Services
- Transcription appears in the text area
- Status shows "Transcripción completada" when done

### 7. Review the Transcription
The transcribed text will appear in the **"📝 Transcripción en Tiempo Real"** text area.

## Expected Behavior

### Successful Transcription
```
Status: Transcripción completada ✅
Textarea: [Your transcribed text here]
```

### No Speech Detected
```
Status: No se detectó voz en el audio
Textarea: [Empty or previous content]
```

### Silence Detected
```
Status: Silencio detectado, finalizando...
Then: Procesando transcripción...
Finally: Transcripción completada
```

### Error Scenario
```
Status: Error: [error message]
Textarea: [Previous content or empty]
```

## Testing Scenarios

### Test 1: Basic Transcription
1. Click "Iniciar Grabación"
2. Say: "Hola, esto es una prueba de transcripción"
3. Click "Detener"
4. Verify transcription appears

### Test 2: Silence Detection
1. Click "Iniciar Grabación"
2. Say: "Probando detección de silencio"
3. Wait silently for 1 second
4. Verify recording stops automatically
5. Verify transcription appears

### Test 3: Long Recording
1. Click "Iniciar Grabación"
2. Speak continuously for 10-15 seconds
3. Click "Detener"
4. Verify full transcription

### Test 4: Multiple Short Phrases
1. Click "Iniciar Grabación"
2. Say: "Primera frase"
3. Pause 0.5 seconds (less than threshold)
4. Say: "Segunda frase"
5. Pause 0.5 seconds
6. Say: "Tercera frase"
7. Click "Detener" or wait 1 second
8. Verify all phrases transcribed

### Test 5: Very Quiet Audio
1. Click "Iniciar Grabación"
2. Speak very quietly or from far away
3. Verify silence detection triggers
4. Check error message about silence

## Debugging

### Check Browser Console
Open browser developer tools (F12) and check the Console tab for:
- Session ID creation
- Chunk sending status
- Any JavaScript errors

### Check Backend Logs
In the terminal where you ran `mvn spring-boot:run`, check for:
- Session start/stop logs
- Audio chunk processing
- Azure API calls
- Any exceptions

### Common Issues

#### Issue: Microphone not working
**Solution**: 
- Check browser permissions
- Verify microphone is connected
- Try a different browser

#### Issue: No transcription appears
**Possible causes**:
- Azure credentials not configured
- Network connectivity issues
- Audio too quiet or no speech detected
**Solution**: Check application.properties and backend logs

#### Issue: Recording doesn't stop on silence
**Possible causes**:
- Background noise too loud
- Microphone sensitivity too high
**Solution**: Adjust silenceThreshold in app.js (currently 1000ms)

#### Issue: Transcription is wrong
**Possible causes**:
- Wrong language configured
- Poor audio quality
- Background noise
**Solution**: 
- Check Azure language setting
- Use higher quality audio settings
- Record in quiet environment

## Advanced Testing

### Testing Thread Safety
Run multiple sessions rapidly:
1. Start recording
2. Speak for 1 second
3. Stop
4. Immediately start again
5. Repeat 10 times
6. Verify no errors occur

### Testing Buffer Management
1. Record for 30+ seconds
2. Verify memory doesn't grow excessively
3. Check buffer is cleared after transcription

### Testing Error Recovery
1. Disconnect network
2. Start recording
3. Try to stop
4. Verify graceful error handling
5. Reconnect network
6. Start new recording
7. Verify it works

## Performance Expectations

- **Chunk send interval**: ~250ms
- **Silence detection check**: ~100ms
- **Auto-stop on silence**: ~1 second
- **Transcription processing**: 1-3 seconds (depends on audio length and Azure response time)

## Browser Compatibility

Tested and working on:
- ✅ Google Chrome 90+
- ✅ Mozilla Firefox 88+
- ✅ Microsoft Edge 90+
- ✅ Safari 14+

## Notes

1. The feature requires a stable internet connection for Azure API calls
2. Transcription accuracy depends on:
   - Audio quality
   - Language match
   - Background noise levels
   - Speaker clarity
3. Session state is managed client-side; refreshing the page resets everything
4. Only one recording session at a time is supported per browser instance

## Support

For issues or questions:
1. Check the console logs (browser and backend)
2. Review REALTIME_TRANSCRIPTION.md for technical details
3. Check IMPLEMENTATION_SUMMARY_REALTIME.md for implementation details
