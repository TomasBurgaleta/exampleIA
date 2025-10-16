
# ExampleIA - Azure Audio Listening Service

## Descripción

Este proyecto implementa un servicio de escucha de audio utilizando Azure Speech Services, siguiendo la arquitectura Clean Code/Hexagonal con Maven multi-módulo.

## Arquitectura

El proyecto está estructurado en 4 capas principales:

- **domain**: Entidades de negocio y contratos (MIObject, AudioListenerPort)
- **application**: Casos de uso y servicios de aplicación (AudioListenerService)
- **infrastructure**: Adaptadores externos (Azure Speech Services)
- **web**: Controladores REST y punto de entrada de la aplicación

## Funcionalidad Principal

El servicio expone la interfaz `byte[] listenAudio(MIObject object)` que:

1. Recibe un objeto MIObject que contiene audio en formato WAV como byte[]
2. Utiliza Azure Speech Services para transcribir el audio
3. Guarda el texto transcrito dentro del MIObject
4. Retorna los datos de audio originales

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
- Suscripción a Azure Speech Services

### Variables de Entorno

```bash
export AZURE_SPEECH_KEY=tu-clave-de-azure
export AZURE_SPEECH_REGION=westeurope
export AZURE_SPEECH_LANGUAGE=es-ES
```

### Configuración en application.properties

```properties
azure.speech.subscription-key=${AZURE_SPEECH_KEY:your-subscription-key-here}
azure.speech.region=${AZURE_SPEECH_REGION:westeurope}
azure.speech.language=${AZURE_SPEECH_LANGUAGE:es-ES}
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
