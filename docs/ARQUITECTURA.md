# Arquitectura de TaskFlow

Bienvenido a TaskFlow. Este documento explica la estructura del proyecto y los conceptos clave para un desarrollador nuevo.

## Capas y paquetes

- Paquete raíz: `com.taskflow`
- Controller (HTTP): `com.taskflow.controller`
  - Ej.: `TaskController` — `src/main/java/com/taskflow/controller/TaskController.java`
  - Ej.: `AuthController` — `src/main/java/com/taskflow/controller/AuthController.java`
- Service (casos de uso): `com.taskflow.service`
  - Ej.: `TaskService` — `src/main/java/com/taskflow/service/TaskService.java`
  - Ej.: `ProjectService` — `src/main/java/com/taskflow/service/ProjectService.java`
- Repository (Spring Data JPA): `com.taskflow.repository`
  - Ej.: `TaskRepository` — `src/main/java/com/taskflow/repository/TaskRepository.java`
  - Ej.: `ProjectRepository` — `src/main/java/com/taskflow/repository/ProjectRepository.java`
- Model (entidades con comportamiento): `com.taskflow.model`
  - Ej.: `Task` — `src/main/java/com/taskflow/model/Task.java`
  - Ej.: `Project` — `src/main/java/com/taskflow/model/Project.java`
- DTOs: `com.taskflow.dto`
  - Ej.: `TaskCreateRequest` — `src/main/java/com/taskflow/dto/TaskCreateRequest.java`
  - Ej.: `TaskResponse` — `src/main/java/com/taskflow/dto/TaskResponse.java`
- Mappers: `com.taskflow.mapper`
  - Ej.: `TaskMapper` — `src/main/java/com/taskflow/mapper/TaskMapper.java`
- Seguridad: `com.taskflow.security`
  - Ej.: `SecurityConfig` — `src/main/java/com/taskflow/security/SecurityConfig.java`
  - Ej.: `JwtService` — `src/main/java/com/taskflow/security/JwtService.java`
  - Ej.: `JwtTokenFilter` — `src/main/java/com/taskflow/security/JwtTokenFilter.java`
  - Ej.: `ProjectSecurity` — `src/main/java/com/taskflow/security/ProjectSecurity.java`
- Excepciones y manejo global: `com.taskflow.exception`
  - Ej.: `GlobalExceptionHandler` — `src/main/java/com/taskflow/exception/GlobalExceptionHandler.java`
- Inicialización de datos (perfil `h2`): `DataSeeder` — `src/main/java/com/taskflow/DataSeeder.java`

## Recorrido de la petición POST /projects/{projectId}/tasks

1. El cliente hace POST a `/projects/{projectId}/tasks` con un `TaskRequest` (DTO) validado por Bean Validation (`@Valid`). Endpoint en `TaskController` (`src/main/java/com/taskflow/controller/TaskController.java`).

2. En `TaskController.createTask(...)` se comprueba que el proyecto existe llamando a `projectService.buscarPorId(projectId)` (`src/main/java/com/taskflow/service/ProjectService.java`). Si no existe se lanza `ProjectNotFoundException` (404). Tras esa comprobación el controlador delega al servicio con `taskService.crear(request, projectId)`.

3. En `TaskService.crear(...)`:
   - Se convierte el DTO a entidad llamando a `TaskMapper.aEntidadNueva(request, projectId)` (`src/main/java/com/taskflow/mapper/TaskMapper.java`), que a su vez utiliza la fábrica de dominio `Task.crear(...)` (`src/main/java/com/taskflow/model/Task.java`) para aplicar reglas de creación (status inicial `TODO`, `dueDate` no en el pasado, etc.).
   - La entidad resultante se persiste con `taskRepository.save(...)` (`src/main/java/com/taskflow/repository/TaskRepository.java`).

4. De vuelta en el controlador, tras persistir se construye la cabecera `Location` y se responde `201 Created` con el cuerpo convertido por `TaskMapper.aResponse(creada)` (`src/main/java/com/taskflow/mapper/TaskMapper.java`).

Notas:
- El controller valida la existencia del proyecto antes de llamar al servicio; el servicio usa el mapper y el dominio para las reglas de creación.
- Evitar duplicar reglas de dominio en el servicio: usar `Task.crear(...)` vía `TaskMapper.aEntidadNueva(...)`.

## Dónde viven las reglas de negocio

- Reglas de entidad y invariantes: en las entidades del dominio (package `com.taskflow.model`). Ejemplos:
  - `Task.crear(...)` y `Task.estaVencida()` en `src/main/java/com/taskflow/model/Task.java`.
  - Estas funciones encapsulan las reglas que siempre deben cumplirse sin importar por dónde se cree la tarea.

- Reglas de caso de uso y coordinación (sagas simples, checks entre entidades, validaciones que necesitan repositorios): en los servicios (`com.taskflow.service`).
  - Ej.: comprobar que un proyecto existe, que el usuario tiene permiso, enviar eventos o actualizar otras entidades.

- Reglas de seguridad y autorización: en `ProjectSecurity` y anotaciones `@PreAuthorize` sobre controladores/servicios (`com/taskflow/security/ProjectSecurity.java` y `SecurityConfig`).

- Validaciones de input (formatos, campos requeridos): en DTOs (`record` con Bean Validation) y en el controlador con `@Valid`.

## Seguridad (JWT)

- Autenticación sin estado: la aplicación usa JWTs. Endpoints públicos: `/auth/**`, `/info`, Swagger, consola H2 y los recursos estáticos de la UI.
  - Controladores públicos: `AuthController` (`src/main/java/com/taskflow/controller/AuthController.java`).

- Flujo básico:
  1. El usuario se autentica en `/auth/login` y recibe un JWT generado por `JwtService` (`src/main/java/com/taskflow/security/JwtService.java`).
  2. El cliente incluye el token en `Authorization: Bearer <token>` en las peticiones siguientes.
  3. `JwtTokenFilter` (`src/main/java/com/taskflow/security/JwtTokenFilter.java`) intercepta la petición, valida el token y crea una `Authentication` en el `SecurityContext`.
  4. Spring Security maneja autorización usando roles/authorities y componentes como `ProjectSecurity` para checks finos (por ejemplo: solo dueño o `ADMIN` puede borrar un proyecto).

- Configuración clave: `SecurityConfig` (`src/main/java/com/taskflow/security/SecurityConfig.java`) declara el filtro, las rutas públicas y la política de sesión (stateless).

## Organización de tests

- Tests unitarios: `src/test/java/com/taskflow/...` — JUnit 5 + Mockito, pruebas puras de lógica en `service` y del `model` sin arrancar Spring. Nombres claros por clase: `TaskServiceTest`, `TaskTest`.

- Slice tests:
  - `@WebMvcTest` para controladores y validación de contratos HTTP.
  - `@DataJpaTest` para repositorios (h2 embebido en memoria durante estas pruebas).

- Integration tests (`*IT.java`): `@SpringBootTest` con profile `test`. Usan Testcontainers y no se ejecutan en la suite normal a menos que se pase `-Ddocker.tests=true`.
  - Evitar activar Docker en desarrollos locales si no es necesario.

- Comandos habituales (Windows / PowerShell):
  - `mvn -q test` — correr la suite normal (no incluye `*IT.java` con Testcontainers)
  - `mvn -q test "-Dtest=TaskServiceTest"` — ejecutar una clase de test concreta
  - `mvn spring-boot:run "-Dspring-boot.run.profiles=h2"` — arrancar la app con H2 y `DataSeeder` (usuario `ana`,`luis`,`admin` y datos de ejemplo)

## Convenciones importantes (resumen)

- Paquetes por capa: `controller` → `service` → `repository` → `model`.
- Reglas de dominio dentro de las entidades (`Task`, `Project`). Reutilizar, no duplicar.
- DTOs como `record` con validación y `@Valid` en controladores.
- Inyección por constructor; sin Lombok.
- Respuestas: `POST` que crea → `201 Created` + `Location`; `DELETE` → `204 No Content`.
- Manejo de errores: `GlobalExceptionHandler` (`src/main/java/com/taskflow/exception/GlobalExceptionHandler.java`) traduce excepciones a respuestas HTTP uniformes (400, 404, 422, 409, 401, 403).
- Seguridad: tokens JWT, rutas públicas limitadas, autorización por roles y por `ProjectSecurity`.

---

Si se necesita, se pueden añadir diagramas (archivos en `docs/`) o ejemplos de llamadas curl. Para cambios en la arquitectura o excepciones a estas convenciones, documentar aquí antes de implementar.
