# Copilot Instructions: Clean Code Maven Project Structure

Este repositorio sigue la arquitectura Clean Code/Hexagonal/Clean Architecture, con separación por capas y gestión de dependencias mediante Maven.  
Sigue estas instrucciones para mantener la coherencia y calidad en todo el proyecto.

---

## Estructura General del Proyecto

- Todo el proyecto es un **Maven multi-module**.
- Cada capa tiene su propio módulo Maven:
  - `domain`: Lógica de negocio, entidades, interfaces (puertos).
  - `application`: Casos de uso, servicios de aplicación.
  - `infrastructure`: Adaptadores externos (DB, APIs, etc.), frameworks, implementaciones técnicas.
  - `web` o `api`: Controladores REST/HTTP, endpoints públicos.

### Ejemplo de estructura de carpetas:
```
/pom.xml
/domain
  /src/main/java/com/tuempresa/tuapp/domain/...
  /pom.xml
/application
  /src/main/java/com/tuempresa/tuapp/application/...
  /pom.xml
/infrastructure
  /src/main/java/com/tuempresa/tuapp/infrastructure/...
  /pom.xml
/web
  /src/main/java/com/tuempresa/tuapp/web/...
  /pom.xml
```

---

## Principios para escribir código en este proyecto

1. **Dominio primero**
   - El módulo `domain` es independiente de frameworks o tecnologías externas.
   - Contiene solo entidades, value objects, repositorios (interfaces), y reglas de negocio puras.

2. **Inversión de dependencias**
   - Las dependencias fluyen hacia el dominio: los módulos externos (infraestructura, API, etc.) dependen del dominio, nunca al revés.
   - Usa interfaces en el dominio; las implementaciones van en infraestructura.

3. **Inyección de dependencias**
   - Utiliza inyección de dependencias (por ejemplo, Spring, Jakarta EE) solo en la capa de infraestructura/web, nunca en el dominio.

4. **Separación de casos de uso**
   - Los servicios de aplicación (en `application`) orquestan la lógica de dominio llamando a entidades/repositories.
   - No mezclar lógica de negocio en controladores ni en adaptadores.

5. **Pruebas**
   - Pruebas de unidad en cada módulo según la responsabilidad.
   - Pruebas de integración en infraestructura/web.

---

## Guía para crear nuevos módulos, paquetes o clases

- **Nuevos casos de uso**:  
  Crea un servicio en `application` y define el contrato en una interfaz.  
  Implementa adaptadores en `infrastructure` si interactúa con sistemas externos.

- **Nuevas entidades/value objects**:  
  Añádelos solo en `domain`. No exponer detalles técnicos.

- **Adaptadores (DB, APIs, etc.)**:  
  Implementa las interfaces del dominio en `infrastructure`.

- **Controladores web/API**:  
  Solo en `web`, orquestando casos de uso mediante servicios de aplicación.

---

## Ejemplo de dependencias en pom.xml

- `web` y `infrastructure` dependen de `application` y `domain`.
- `application` depende de `domain`.
- `domain` no depende de ningún otro módulo.

```xml
<!-- Ejemplo para module web/pom.xml -->
<dependencies>
  <dependency>
    <groupId>com.tuempresa.tuapp</groupId>
    <artifactId>application</artifactId>
    <version>${project.version}</version>
  </dependency>
  <dependency>
    <groupId>com.tuempresa.tuapp</groupId>
    <artifactId>domain</artifactId>
    <version>${project.version}</version>
  </dependency>
  <!-- Dependencias externas/framework aquí -->
</dependencies>
```

---

## Estilo de código y convenciones

- Paquetería: `com.tuempresa.tuapp.[layer]`
- Nombres claros y descriptivos.
- Sin lógica técnica en el dominio.
- Usa DTOs solo para entrada/salida de controladores.

---

## Ejemplo de código mínimo

**Entidad en domain:**
```java
package com.tuempresa.tuapp.domain.model;

public class Usuario {
    private final String id;
    private final String nombre;
    // Constructor, getters, lógica de negocio...
}
```

**Repositorio (puerto) en domain:**
```java
package com.tuempresa.tuapp.domain.repository;

public interface UsuarioRepository {
    Usuario buscarPorId(String id);
    void guardar(Usuario usuario);
}
```

**Caso de uso en application:**
```java
package com.tuempresa.tuapp.application.service;

public class CrearUsuarioService {
    private final UsuarioRepository usuarioRepository;

    public CrearUsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void ejecutar(CrearUsuarioCommand command) {
        // Lógica de orquestación
    }
}
```

**Adaptador en infrastructure:**
```java
package com.tuempresa.tuapp.infrastructure.repository;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {
    // Implementación usando JPA/JDBC/otro
}
```

---

## Notas para Copilot

- Siempre sugerir la ubicación del archivo según la capa correspondiente.
- Mantener la independencia del dominio respecto a frameworks.
- Sugerir test unitarios junto con cada clase de dominio y aplicación.
- Seguir la nomenclatura de paquetes y clases.

---

¿Dudas? Consulta este archivo o revisa la estructura del repositorio actual.
