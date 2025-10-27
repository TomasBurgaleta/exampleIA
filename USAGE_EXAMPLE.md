# Ejemplo de Uso - Deepgram Speech-to-Text

Este documento proporciona ejemplos prácticos de cómo utilizar el servicio con Deepgram.

## Configuración Rápida

### 1. Obtener API Key de Deepgram

1. Regístrate en [https://deepgram.com](https://deepgram.com)
2. Crea un proyecto
3. Genera una API Key

### 2. Configurar el Proyecto

Edita `web/src/main/resources/application.properties`:

```properties
# Seleccionar Deepgram como proveedor
speech.provider=deepgram

# Configuración de Deepgram
deepgram.speech.api-key=tu-clave-api-aqui
deepgram.speech.model=nova-2
deepgram.speech.language=es
```

O usando variables de entorno:

```bash
export SPEECH_PROVIDER=deepgram
export DEEPGRAM_API_KEY=tu-clave-api-aqui
export DEEPGRAM_MODEL=nova-2
export DEEPGRAM_LANGUAGE=es
```

### 3. Ejecutar la Aplicación

```bash
mvn spring-boot:run -pl web
```

## Ejemplo de Uso con cURL

### Transcribir un archivo de audio

```bash
curl -X POST http://localhost:8080/api/audio/transcribe \
  -H "Content-Type: multipart/form-data" \
  -F "file=@mi_audio.wav"
```

### Respuesta Esperada

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "transcribedText": "Hola, este es un ejemplo de transcripción con Deepgram.",
  "detectedLanguage": "es",
  "aiResponse": "¡Hola! Es un placer ayudarte con la transcripción de audio...",
  "hasAiResponse": true,
  "audioSize": 12345,
  "hasTranscription": true
}
```

## Ejemplo de Uso Programático

### Java

```java
@Autowired
private AudioListenerService audioListenerService;

public void transcribeAudio() {
    // Crear AudioBean con datos de audio
    byte[] audioData = Files.readAllBytes(Paths.get("audio.wav"));
    AudioBean audioBean = new AudioBean("audio-1", audioData);
    audioBean.setSamplesPerSecond(16000);
    audioBean.setBitsPerSample((short) 16);
    audioBean.setChannels((short) 1);
    
    try {
        // Transcribir con el proveedor configurado (Deepgram en este caso)
        audioListenerService.listenAudio(audioBean);
        
        // Obtener resultado
        String transcription = audioBean.getTranscribedText();
        String language = audioBean.getDetectedLanguage();
        
        System.out.println("Transcripción: " + transcription);
        System.out.println("Idioma: " + language);
    } catch (AudioProcessingException e) {
        System.err.println("Error: " + e.getMessage());
    }
}
```

## Cambiar entre Proveedores

### Usar Azure

```properties
speech.provider=azure
azure.speech.subscription-key=tu-clave-azure
azure.speech.region=westeurope
azure.speech.language=es-ES
```

### Usar Deepgram

```properties
speech.provider=deepgram
deepgram.speech.api-key=tu-clave-deepgram
deepgram.speech.model=nova-2
deepgram.speech.language=es
```

No se requieren cambios en el código - solo en la configuración!

## Pruebas

### Probar con Azure

```bash
export SPEECH_PROVIDER=azure
export AZURE_SPEECH_KEY=tu-clave-azure
export AZURE_SPEECH_REGION=westeurope
mvn spring-boot:run -pl web
```

### Probar con Deepgram

```bash
export SPEECH_PROVIDER=deepgram
export DEEPGRAM_API_KEY=tu-clave-deepgram
mvn spring-boot:run -pl web
```

## Comparación de Características

| Característica | Azure | Deepgram |
|----------------|-------|----------|
| Transcripción en tiempo real | ✅ | ✅ |
| Detección de idioma | ✅ | ✅ |
| Puntuación automática | ✅ | ✅ |
| Soporte multi-idioma | ✅ | ✅ |
| SDK nativo | ✅ | ❌ (REST API) |
| Modelo personalizable | ✅ | ✅ |

## Solución de Problemas

### Deepgram no funciona

1. Verifica la API key:
   ```bash
   echo $DEEPGRAM_API_KEY
   ```

2. Verifica la configuración:
   ```bash
   grep deepgram web/src/main/resources/application.properties
   ```

3. Verifica los logs:
   ```bash
   mvn spring-boot:run -pl web | grep -i deepgram
   ```

### Azure no funciona

1. Verifica la subscription key y región
2. Asegúrate de que `speech.provider=azure`
3. Revisa los logs de Azure

## Recursos Adicionales

- [Documentación completa de Deepgram](DEEPGRAM_INTEGRATION.md)
- [Documentación de la API de Deepgram](https://developers.deepgram.com/docs)
- [README del proyecto](README.md)
