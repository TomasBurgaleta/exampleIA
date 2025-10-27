
# ExampleIA - Multi-Provider Speech-to-Text Service with OpenAI Integration

## Descripción

Este proyecto implementa un servicio de escucha de audio con soporte para **múltiples proveedores de speech-to-text** (Azure Speech Services y Deepgram) y OpenAI, siguiendo la arquitectura Clean Code/Hexagonal con Maven multi-módulo.

## Arquitectura

El proyecto está estructurado en 4 capas principales:

- **domain**: Entidades de negocio y contratos (AudioBean, SpeechToTextPort, AudioListenerPort, AiServicePort)
- **application**: Casos de uso y servicios de aplicación (AudioListenerService, AudioRecordingService)
- **infrastructure**: Adaptadores externos (Azure Speech Services, Deepgram, OpenAI)
- **web**: Controladores REST y punto de entrada de la aplicación

## Funcionalidad Principal

### Transcripción de Audio - Multi-Proveedor

**NUEVO**: El sistema ahora soporta múltiples proveedores de speech-to-text:
- **Azure Speech Services** (proveedor por defecto)
- **Deepgram** (alternativa mediante REST API)

El servicio expone la interfaz común `SpeechToTextPort` que:

1. Recibe un objeto AudioBean que contiene audio en formato WAV como byte[]
2. Utiliza el proveedor configurado (Azure o Deepgram) para transcribir el audio
3. Guarda el texto transcrito dentro del AudioBean
4. Detecta automáticamente el idioma del audio
5. Retorna los datos de audio originales

Ver [DEEPGRAM_INTEGRATION.md](DEEPGRAM_INTEGRATION.md) para documentación completa sobre la integración multi-proveedor.

### Integración con OpenAI

**NUEVO**: El sistema ahora integra OpenAI para proporcionar respuestas inteligentes basadas en el texto transcrito:

1. Una vez transcrito el audio con Azure Speech Services
2. El texto se envía automáticamente a OpenAI (gpt-3.5-turbo por defecto)
3. La respuesta de la IA se muestra junto con la transcripción en la interfaz web

Ver [OPENAI_INTEGRATION.md](OPENAI_INTEGRATION.md) para documentación completa sobre la integración con OpenAI.

### Detección de Silencio

El sistema incluye **detección automática de silencio** implementada en Java que:

1. Analiza los datos PCM del audio grabado
2. Calcula la amplitud RMS (Root Mean Square) de las muestras
3. Determina si el audio contiene silencio (>95% de muestras por debajo del umbral)
4. Informa al usuario si se detecta silencio, evitando el procesamiento innecesario

Ver [SILENCE_DETECTION.md](SILENCE_DETECTION.md) para más detalles sobre esta funcionalidad.

## Configuración

### Requisitos

- Java 17 o superior
- Maven 3.6 o superior
- Suscripción a Azure Speech Services (si usa Azure como proveedor)
- API Key de Deepgram (si usa Deepgram como proveedor)
- API Key de OpenAI

### Variables de Entorno

#### Para Azure (proveedor por defecto)
```bash
export AZURE_SPEECH_KEY=tu-clave-de-azure
export AZURE_SPEECH_REGION=westeurope
export AZURE_SPEECH_LANGUAGE=es-ES
export OPENAI_API_KEY=tu-clave-de-openai
```

#### Para Deepgram
```bash
export SPEECH_PROVIDER=deepgram
export DEEPGRAM_API_KEY=tu-clave-de-deepgram
export DEEPGRAM_MODEL=nova-2
export DEEPGRAM_LANGUAGE=es
export OPENAI_API_KEY=tu-clave-de-openai
```

### Configuración en application.properties

```properties
# Speech-to-Text Provider Selection (azure or deepgram)
speech.provider=${SPEECH_PROVIDER:azure}

# Azure Speech Services
azure.speech.subscription-key=${AZURE_SPEECH_KEY:your-subscription-key-here}
azure.speech.region=${AZURE_SPEECH_REGION:westeurope}
azure.speech.language=${AZURE_SPEECH_LANGUAGE:es-ES}

# Deepgram Speech Services
deepgram.speech.api-key=${DEEPGRAM_API_KEY:your-deepgram-api-key-here}
deepgram.speech.model=${DEEPGRAM_MODEL:nova-2}
deepgram.speech.language=${DEEPGRAM_LANGUAGE:es}

# OpenAI Configuration
openai.api-key=${OPENAI_API_KEY:your-openai-api-key-here}
openai.model=${OPENAI_MODEL:gpt-3.5-turbo}
openai.temperature=${OPENAI_TEMPERATURE:0.7}
openai.max-tokens=${OPENAI_MAX_TOKENS:150}
openai.log-requests=${OPENAI_LOG_REQUESTS:true}
openai.log-responses=${OPENAI_LOG_RESPONSES:true}
```

## Compilación y Ejecución

### Compilar el proyecto

```bash
mvn clean compile
```

### Ejecutar los tests

```bash
mvn test
```

### Ejecutar la aplicación

```bash
mvn spring-boot:run -pl web
```

## Uso de la API

### Endpoint de Transcripción

**POST** `/api/audio/transcribe`

- Content-Type: `multipart/form-data`
- Parámetro: `file` (archivo WAV)

**Ejemplo con curl:**

```bash
curl -X POST \
  http://localhost:8080/api/audio/transcribe \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@audio.wav'
```

**Respuesta:**

```json
{
  "id": "uuid-del-objeto",
  "transcribedText": "Texto transcrito del audio",
  "aiResponse": "Respuesta generada por OpenAI basada en el texto",
  "hasAiResponse": true,
  "audioSize": 12345,
  "hasTranscription": true
}
```

### Endpoint de Salud

**GET** `/api/audio/health`

```json
{
  "status": "UP",
  "service": "Azure Audio Listening Service"
}
```

## Estructura del Proyecto

```
├── pom.xml                 # POM padre del multi-módulo
├── domain/                 # Capa de dominio
│   ├── src/main/java/
│   │   └── com/tomasburgaleta/exampleia/domain/
│   │       ├── model/
│   │       │   └── MIObject.java
│   │       └── port/
│   │           ├── AudioListenerPort.java
│   │           └── AudioProcessingException.java
│   └── src/test/java/
├── application/            # Capa de aplicación
│   ├── src/main/java/
│   │   └── com/tomasburgaleta/exampleia/application/
│   │       └── service/
│   │           └── AudioListenerService.java
│   └── src/test/java/
├── infrastructure/         # Capa de infraestructura
│   ├── src/main/java/
│   │   └── com/tomasburgaleta/exampleia/infrastructure/
│   │       ├── adapter/
│   │       │   └── AzureAudioListenerAdapter.java
│   │       └── config/
│   │           └── AzureSpeechConfig.java
│   └── src/test/java/
└── web/                    # Capa web
    ├── src/main/java/
    │   └── com/tomasburgaleta/exampleia/web/
    │       ├── ExampleIAApplication.java
    │       ├── config/
    │       │   └── ApplicationConfig.java
    │       └── controller/
    │           └── AudioController.java
    └── src/main/resources/
        └── application.properties
```

## Notas Técnicas

- El servicio maneja archivos WAV de hasta 10MB
- La transcripción se realiza de forma síncrona
- Los errores se manejan mediante excepciones específicas del dominio
- La configuración de Azure es validada al inicializar el adaptador
- Se incluyen logs detallados para debugging

## Testing

El proyecto incluye tests unitarios para las capas de dominio y aplicación. Para ejecutar todos los tests:

```bash
mvn test
```

## Principios Aplicados

- **Clean Architecture**: Separación clara entre capas con dependencias unidireccionales
- **Hexagonal Architecture**: Uso de puertos y adaptadores para aislar la lógica de negocio
- **Dependency Inversion**: Las dependencias fluyen hacia el dominio
- **Single Responsibility**: Cada clase tiene una única responsabilidad bien definida
