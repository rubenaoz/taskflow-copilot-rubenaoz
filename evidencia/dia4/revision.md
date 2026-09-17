# Copilot CLI Session

> [!NOTE]
> - **Session ID:** `148cbc5b-3f06-4357-9f1d-2cf3a872db23`  
> - **Started:** 9/17/2026, 12:24:28 PM  
> - **Duration:** 43s  
> - **Exported:** 9/17/2026, 12:25:12 PM  

---

<sub>1s</sub>

### User

Revisa evidencia/dia4/summary.diff contra la especificación issues/summary.md.

---

<sub>4s</sub>

### Copilot

Reading the diff and the spec, then opening each file modified in the diff to compare content and run the checklist. Running parallel file reads: evidence/dia4/summary.diff and issues/summary.md, then will open any files referenced by the diff.

---

<sub>4s</sub>

### `view`

**view the file at C:\Users\PC\Documents\taskflow-copilot-rubenaoz\evidencia\dia4\summary.diff.**

C:\Users\PC\Documents\taskflow-copilot-rubenaoz\evidencia\dia4\summary.diff

<details>
<summary>76 lines</summary>

```diff
diff --git a/src/main/java/com/taskflow/controller/ProjectController.java b/src/main/java/com/taskflow/controller/ProjectController.java
index 56ccccf..4a57e95 100644
--- a/src/main/java/com/taskflow/controller/ProjectController.java
+++ b/src/main/java/com/taskflow/controller/ProjectController.java
@@ -66,6 +66,19 @@ public class ProjectController {
         return ProjectMapper.aResponse(proyecto);
     }
 
+    /**
+     * GET /projects/{id}/summary — resumen del proyecto: conteos por estado y vencidas.
+     * 404 se maneja igual que en getProject (ProjectNotFoundException).
+     */
+    @Operation(summary = "Resumen del proyecto",
+            description = "Devuelve un resumen con total de tareas, conteo por estado y vencidas.")
+    @GetMapping("/projects/{id}/summary")
+    public com.taskflow.dto.ProjectSummaryResponse getProjectSummary(@PathVariable("id") Long id) {
+        Project proyecto = projectService.buscarPorId(id)
+                .orElseThrow(() -> new ProjectNotFoundException(id));
+        return projectService.resumenDe(proyecto);
+    }
+
     /**
      * GET /projects/{id}/tasks — las tareas de un proyecto como TaskResponse. Conserva la distinción
      * de D2: proyecto inexistente -> 404 (orElseThrow); proyecto sin tareas -> 200 con []. El filtro
diff --git a/src/main/java/com/taskflow/mapper/ProjectMapper.java b/src/main/java/com/taskflow/mapper/ProjectMapper.java
index a33a95b..418b758 100644
--- a/src/main/java/com/taskflow/mapper/ProjectMapper.java
+++ b/src/main/java/com/taskflow/mapper/ProjectMapper.java
@@ -22,4 +22,19 @@ public final class ProjectMapper {
         return new ProjectResponse(p.getId(), p.getName(), p.getDescription(),
                 p.getOwnerId(), p.getCreatedAt());
     }
+
+    /**
+     * Convierte un Project y conteos en un ProjectSummaryResponse. Convierte las claves de TaskStatus
+     * a String para el DTO.
+     */
+    public static com.taskflow.dto.ProjectSummaryResponse aSummaryResponse(Project p,
+                                                                           int totalTasks,
+                                                                           java.util.Map<com.taskflow.model.TaskStatus, Integer> byStatus,
+                                                                           int overdue) {
+        java.util.Map<String, Integer> byStatusString = new java.util.HashMap<>();
+        for (com.taskflow.model.TaskStatus s : com.taskflow.model.TaskStatus.values()) {
+            byStatusString.put(s.name(), byStatus.getOrDefault(s, 0));
+        }
+        return new com.taskflow.dto.ProjectSummaryResponse(p.getId(), p.getName(), totalTasks, byStatusString, overdue);
+    }
 }
diff --git a/src/main/java/com/taskflow/service/ProjectService.java b/src/main/java/com/taskflow/service/ProjectService.java
index d315e3f..2bf7830 100644
--- a/src/main/java/com/taskflow/service/ProjectService.java
+++ b/src/main/java/com/taskflow/service/ProjectService.java
@@ -62,6 +62,25 @@ public class ProjectService {
         return taskRepository.findByProjectId(projectId);
     }
 
+    /**
+     * Construye el resumen de un proyecto: cuenta tareas por estado, total y vencidas.
+     * El controller decide el 404 (buscarPorId) y pasa el Project ya existente.
+     */
+    public com.taskflow.dto.ProjectSummaryResponse resumenDe(Project project) {
+        List<Task> tareas = tareasDe(project.getId());
+        int total = tareas.size();
+        java.util.Map<com.taskflow.model.TaskStatus, Integer> byStatus = new java.util.HashMap<>();
+        for (com.taskflow.model.TaskStatus s : com.taskflow.model.TaskStatus.values()) {
+            byStatus.put(s, 0);
+        }
+        int overdue = 0;
+        for (Task t : tareas) {
+            byStatus.put(t.getStatus(), byStatus.getOrDefault(t.getStatus(), 0) + 1);
+            if (t.estaVencida()) overdue++;
+        }
+        return com.taskflow.mapper.ProjectMapper.aSummaryResponse(project, total, byStatus, overdue);
+    }
+
     /**
      * Crea un proyecto (POST): el request trae name y description; el ownerId ya NO es una constante
      * (murió el 1L fijo de D3) — se resuelve del USERNAME AUTENTICADO (el que puso el JWT en el
```

</details>


---

<sub>4s</sub>

### `view`

**view the file at C:\Users\PC\Documents\taskflow-copilot-rubenaoz\issues\summary.md.**

C:\Users\PC\Documents\taskflow-copilot-rubenaoz\issues\summary.md

<details>
<summary>52 lines</summary>

````
## Qué se pide

Un endpoint nuevo, `GET /projects/{id}/summary`, que resume un proyecto: **cuántas tareas tiene en
cada estado y cuántas están vencidas**. Sirve para pintar un tablero sin descargar todas las tareas del
proyecto. Pide el token de `POST /auth/login`, como todo lo que no es público.

## Respuesta 200

```json
{
  "projectId": 2,
  "projectName": "App Móvil",
  "totalTasks": 4,
  "byStatus": { "TODO": 1, "IN_PROGRESS": 2, "DONE": 1 },
  "overdue": 1
}
```

| Campo | Tipo | Qué es |
|---|---|---|
| `projectId` | número | el `id` del proyecto pedido |
| `projectName` | texto | el `name` del proyecto |
| `totalTasks` | número | cuántas tareas tiene el proyecto |
| `byStatus` | objeto | una clave por cada valor de `TaskStatus` (`TODO`, `IN_PROGRESS`, `DONE`), **siempre las tres**, aunque valgan 0 |
| `overdue` | número | cuántas tareas del proyecto cumplen `Task.estaVencida()` |

## Reglas

1. **Vencida** es exactamente lo que ya dice `Task.estaVencida()`: tiene fecha límite, la fecha ya pasó y
   el estado no es `DONE`. Se reutiliza ese método; no se reescribe la regla en el servicio.
2. Un proyecto **sin tareas** responde 200 con `totalTasks` 0, las tres claves de `byStatus` en 0 y `overdue` 0.
3. Un proyecto **que no existe** responde **404** con el `ErrorResponse` uniforme de siempre
   (`ProjectNotFoundException`, igual que `GET /projects/{id}`).
4. **Sin token** responde **401**, como todo lo que no es público.
5. Cualquier usuario autenticado puede pedir el resumen de cualquier proyecto (igual que `GET /projects/{id}/tasks`).

## Resultado esperado con la semilla del perfil `h2`

| Petición | `totalTasks` | `byStatus` | `overdue` |
|---|---|---|---|
| `GET /projects/1/summary` | 5 | `TODO` 3 · `IN_PROGRESS` 1 · `DONE` 1 | 0 (la tarea 2 tiene fecha pasada, pero está `DONE`) |
| `GET /projects/2/summary` | 4 | `TODO` 1 · `IN_PROGRESS` 2 · `DONE` 1 | 1 (la tarea 7) |
| `GET /projects/3/summary` | 0 | `TODO` 0 · `IN_PROGRESS` 0 · `DONE` 0 | 0 |
| `GET /projects/99/summary` | — | — | 404 |

Si antes de probar creaste tareas en un proyecto (por la UI o con el servidor MCP), `totalTasks` y
`TODO` de ese proyecto suben en la misma cantidad. Al reiniciar la app con el perfil `h2` vuelven a
ser los de la tabla.

## Cómo se construye (convenciones de TaskFlow)

- DTO de salida: un `record` en `com.taskflow.dto`, `ProjectSummaryResponse`.
- La lógica de conteo va en el servicio (`ProjectService`), no en el controller.
- El controller solo traduce HTTP: busca el proyecto (404 si no existe) y delega.
- Tests:
  - **unit** del servicio con Mockito: un proyecto con tareas en los tres estados y una vencida; un proyecto sin tareas.
  - **slice** `@WebMvcTest` del controller: 200 con el JSON de arriba y 404 con un proyecto inexistente.
- `mvn -q test` en verde al terminar, sin modificar tests existentes.

## Criterios de aceptación

- [ ] `GET /projects/2/summary` con el token de `ana` devuelve el JSON del ejemplo.
- [ ] `GET /projects/3/summary` devuelve ceros en los cinco números.
- [ ] `GET /projects/99/summary` devuelve 404.
- [ ] `mvn -q test` en verde: los tests de antes más los nuevos.
````

</details>


---

<sub>11s</sub>

### Copilot

Leyendo las versiones actuales de los archivos modificados (ProjectController, ProjectMapper, ProjectService) para comparar el diff con la especificación y aplicar la checklist de revisión. Ejecutando lecturas en paralelo.

---

<sub>11s</sub>

### `view`

**view the file at C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\controller\ProjectController.java.**

C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\controller\ProjectController.java

<details>
<summary>135 lines</summary>

```
package com.taskflow.controller;

import com.taskflow.dto.ProjectRequest;
import com.taskflow.dto.ProjectResponse;
import com.taskflow.dto.TaskResponse;
import com.taskflow.exception.ProjectNotFoundException;
import com.taskflow.mapper.ProjectMapper;
import com.taskflow.mapper.TaskMapper;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * ProjectController — la puerta HTTP de los proyectos. HOY (integrador) crece a CRUD completo, en
 * DTOs (ProjectRequest / ProjectResponse); las tareas de un proyecto salen como TaskResponse.
 *
 * Repetición deliberada del patrón de TaskController: mismos gestos (201 + Location, @Valid, 404 vía
 * orElseThrow -> advice). El lado Project cierra la tabla de endpoints del capstone (menos /auth/*).
 */
@RestController
@Tag(name = "Projects", description = "CRUD de proyectos y sus tareas. Borrar un proyecto arrastra sus tareas (cascada).")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /** GET /projects — todos los proyectos como ProjectResponse. */
    @Operation(summary = "Lista todos los proyectos",
            description = "Devuelve todos los proyectos como ProjectResponse (lista vacía si no hay ninguno).")
    @GetMapping("/projects")
    public List<ProjectResponse> getProjects() {
        return projectService.listar().stream()
                .map(ProjectMapper::aResponse)
                .toList();
    }

    /** GET /projects/{id} — 200 con ProjectResponse; 404 uniforme si no existe. */
    @Operation(summary = "Obtiene un proyecto por id",
            description = "200 con el ProjectResponse; 404 uniforme si el id no existe.")
    @GetMapping("/projects/{id}")
    public ProjectResponse getProject(@PathVariable("id") Long id) {
        Project proyecto = projectService.buscarPorId(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        return ProjectMapper.aResponse(proyecto);
    }

    /**
     * GET /projects/{id}/summary — resumen del proyecto: conteos por estado y vencidas.
     * 404 se maneja igual que en getProject (ProjectNotFoundException).
     */
    @Operation(summary = "Resumen del proyecto",
            description = "Devuelve un resumen con total de tareas, conteo por estado y vencidas.")
    @GetMapping("/projects/{id}/summary")
    public com.taskflow.dto.ProjectSummaryResponse getProjectSummary(@PathVariable("id") Long id) {
        Project proyecto = projectService.buscarPorId(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        return projectService.resumenDe(proyecto);
    }

    /**
     * GET /projects/{id}/tasks — las tareas de un proyecto como TaskResponse. Conserva la distinción
     * de D2: proyecto inexistente -> 404 (orElseThrow); proyecto sin tareas -> 200 con []. El filtro
     * ?status= es STRETCH (mismo enum que /tasks). Sigue delegando en ProjectService.tareasDe.
     */
    @Operation(summary = "Lista las tareas de un proyecto",
            description = "Tareas del proyecto como TaskResponse; 404 si el proyecto no existe, 200 con [] si no tiene tareas. Filtro opcional ?status= (stretch).")
    @GetMapping("/projects/{id}/tasks")
    public List<TaskResponse> getTareasDeProyecto(
            @PathVariable("id") Long id,
            @RequestParam(name = "status", required = false) TaskStatus status) {   // status: STRETCH
        projectService.buscarPorId(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        List<Task> tareas = projectService.tareasDe(id);
        if (status != null) {                          // STRETCH: filtro opcional por estado
            tareas = tareas.stream().filter(t -> t.getStatus() == status).toList();
        }
        return tareas.stream().map(TaskMapper::aResponse).toList();
    }

    /**
     * POST /projects — 201 + Location a /projects/{id}. @Valid dispara Bean Validation (400 si falla).
     * MP-9: el owner sale del JWT — el Authentication (inyectado por Spring Security) trae el username
     * del token; el service lo resuelve a ownerId. El dueño es QUIEN crea, no una constante.
     */
    @Operation(summary = "Crea un proyecto",
            description = "El owner se toma del usuario autenticado (el username del JWT).")
    @PostMapping("/projects")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request,
                                                         Authentication authentication) {
        Project creado = projectService.crear(request, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/projects/{id}")
                .buildAndExpand(creado.getId())
                .toUri();
        return ResponseEntity.created(location).body(ProjectMapper.aResponse(creado));
    }

    /** PUT /projects/{id} — reemplazo COMPLETO. 200 con ProjectResponse; 404 si no existe. */
    @Operation(summary = "Reemplaza un proyecto",
            description = "Conserva id, owner y createdAt; name y description vienen del cuerpo.")
    @PutMapping("/projects/{id}")
    public ProjectResponse updateProject(@PathVariable("id") Long id,
                                         @Valid @RequestBody ProjectRequest request) {
        Project actualizado = projectService.reemplazar(id, request);
        return ProjectMapper.aResponse(actualizado);
    }

    /**
     * DELETE /projects/{id} — 204 No Content; 404 si no existe. Borra en cascada las tareas del
     * proyecto (regla "no Task sin Project").
     *
     * MP-9: la regla del capstone EN SERIO. @PreAuthorize se evalúa ANTES del método: pasa si el
     * usuario es ADMIN o es el owner del proyecto (bean @projectSecurity, data-driven). Un USER que no
     * es owner -> 403 (la API SÍ sabe quién es, por eso 403 y no 401). Proyecto inexistente ->
     * esOwner devuelve true a propósito para que hable el 404 del servicio.
     */
    @Operation(summary = "Borra un proyecto y sus tareas",
            description = "Solo ADMIN o el owner. Un USER no-owner recibe 403.")
    @PreAuthorize("hasRole('ADMIN') or @projectSecurity.esOwner(#id, authentication.name)")
    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable("id") Long id) {
        projectService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
```

</details>


---

<sub>11s</sub>

### `view`

**view the file at C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\mapper\ProjectMapper.java.**

C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\mapper\ProjectMapper.java

<details>
<summary>35 lines</summary>

```
package com.taskflow.mapper;

import com.taskflow.dto.ProjectResponse;
import com.taskflow.model.Project;

/**
 * ProjectMapper — puente DTO &lt;-&gt; dominio del lado Project. Estático, a mano, sin MapStruct.
 *
 * HOY se SIMPLIFICÓ (lo prometía D3): la entidad ya guarda 'ownerId' directo (se aplanó el 'User
 * owner' en MP-4), así que aResponse ya no deriva el id desde un objeto (p.getOwner().id()) — lee
 * p.getOwnerId() tal cual. El contrato de salida (ProjectResponse con ownerId Long) no cambió; el
 * mapeo se volvió trivial porque el dominio por fin coincide con la forma canónica.
 */
public final class ProjectMapper {

    private ProjectMapper() {
        // no instanciable
    }

    /** Entidad -> DTO de salida. Ahora ownerId sale directo del campo (sin puente por objeto). */
    public static ProjectResponse aResponse(Project p) {
        return new ProjectResponse(p.getId(), p.getName(), p.getDescription(),
                p.getOwnerId(), p.getCreatedAt());
    }

    /**
     * Convierte un Project y conteos en un ProjectSummaryResponse. Convierte las claves de TaskStatus
     * a String para el DTO.
     */
    public static com.taskflow.dto.ProjectSummaryResponse aSummaryResponse(Project p,
                                                                           int totalTasks,
                                                                           java.util.Map<com.taskflow.model.TaskStatus, Integer> byStatus,
                                                                           int overdue) {
        java.util.Map<String, Integer> byStatusString = new java.util.HashMap<>();
        for (com.taskflow.model.TaskStatus s : com.taskflow.model.TaskStatus.values()) {
            byStatusString.put(s.name(), byStatus.getOrDefault(s, 0));
        }
        return new com.taskflow.dto.ProjectSummaryResponse(p.getId(), p.getName(), totalTasks, byStatusString, overdue);
    }
}
```

</details>


---

<sub>11s</sub>

### `view`

**view the file at C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\service\ProjectService.java.**

C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\service\ProjectService.java

<details>
<summary>111 lines</summary>

```
package com.taskflow.service;

import com.taskflow.dto.ProjectRequest;
import com.taskflow.exception.ProjectNotFoundException;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ProjectService — la capa de negocio del lado Project. Cero HTTP aquí (el "no existe" se traduce con
 * orElseThrow -> ProjectNotFoundException; el advice le pone el 404).
 *
 * Cambios de HOY (S2D4) — y por qué la promesa del día NO lo cubre:
 *   - MP-4: el modelo Project se aplanó (User owner -> Long ownerId), así que 'crear' y 'reemplazar'
 *     manejan un ownerId (Long), no un objeto User. Antes 'crear' construía con 0L (convención del
 *     InMemory de "aún sin id"); ahora construye con null y la BD asigna el id (IDENTITY).
 *   - MP-6: 'tareasDe' cambió su stream-filter de findAll() por taskRepository.findByProjectId(id)
 *     — la nota de D3 ("en D4 esto se vuelve un query method") se paga AQUÍ. La promesa "no tocar el
 *     servicio" protege a TaskService (que no se tocó), no a ProjectService (que ya se tocó en MP-4).
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;   // D5: para resolver el owner desde el username del JWT

    public ProjectService(ProjectRepository projectRepository, TaskRepository taskRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    /** Todos los proyectos. */
    public List<Project> listar() {
        return projectRepository.findAll();
    }

    /** Un proyecto por id (el Optional sube tal cual: el controller decide 404 vs 200). */
    public Optional<Project> buscarPorId(Long id) {
        return projectRepository.findById(id);
    }

    /**
     * Las tareas de un proyecto: MP-6 sustituye el stream-filter de D2 por la derived query
     * taskRepository.findByProjectId(projectId) — el LIKE... perdón, el WHERE project_id = ? lo hace
     * la BD (viajan solo las filas del proyecto). "no existe el proyecto" (404) lo decide el
     * controller con buscarPorId; ESTE método, si el proyecto existe pero no tiene tareas, devuelve
     * [] (-> 200 con []). No son lo mismo.
     */
    public List<Task> tareasDe(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    /**
     * Construye el resumen de un proyecto: cuenta tareas por estado, total y vencidas.
     * El controller decide el 404 (buscarPorId) y pasa el Project ya existente.
     */
    public com.taskflow.dto.ProjectSummaryResponse resumenDe(Project project) {
        List<Task> tareas = tareasDe(project.getId());
        int total = tareas.size();
        java.util.Map<com.taskflow.model.TaskStatus, Integer> byStatus = new java.util.HashMap<>();
        for (com.taskflow.model.TaskStatus s : com.taskflow.model.TaskStatus.values()) {
            byStatus.put(s, 0);
        }
        int overdue = 0;
        for (Task t : tareas) {
            byStatus.put(t.getStatus(), byStatus.getOrDefault(t.getStatus(), 0) + 1);
            if (t.estaVencida()) overdue++;
        }
        return com.taskflow.mapper.ProjectMapper.aSummaryResponse(project, total, byStatus, overdue);
    }

    /**
     * Crea un proyecto (POST): el request trae name y description; el ownerId ya NO es una constante
     * (murió el 1L fijo de D3) — se resuelve del USERNAME AUTENTICADO (el que puso el JWT en el
     * Authentication). El dueño no lo decide el cliente ni una semilla: es QUIEN llama. createdAt = hoy;
     * id == null -> la BD lo asigna (IDENTITY).
     */
    public Project crear(ProjectRequest request, String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new UsernameNotFoundException(ownerUsername));
        Project nuevo = new Project(null, request.name(), request.description(),
                owner.getId(), LocalDate.now());
        return projectRepository.save(nuevo);
    }

    /**
     * Reemplazo COMPLETO (PUT): existe -> reconstruye con el MISMO id, conservando ownerId y createdAt
     * (el cliente no los toca); name y description vienen del request. save con id presente = update.
     * No existe -> 404.
     */
    public Project reemplazar(Long id, ProjectRequest request) {
        Project actual = projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        Project reemplazo = new Project(actual.getId(), request.name(), request.description(),
                actual.getOwnerId(), actual.getCreatedAt());
        return projectRepository.save(reemplazo);
    }

    /**
     * Elimina un proyecto (DELETE) con CASCADA MANUAL: borra primero sus tareas (regla "no Task sin
     * Project" — y además la FK task.project_id lo EXIGE: borrar el proyecto con tareas colgando daría
     * una violación de integridad) y luego el proyecto. No existe -> 404. La regla owner/ADMIN llega
     * en D5 (hoy queda abierto, documentado en el @Operation del controller).
     */
    public void eliminar(Long id) {
        projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
        tareasDe(id).forEach(t -> taskRepository.deleteById(t.getId()));   // cascada manual (la FK obliga el orden)
        projectRepository.deleteById(id);
    }
}
```

</details>


---

<sub>15s</sub>

### Copilot

Buscando el DTO ProjectSummaryResponse y tests relacionados abriendo el paquete de DTOs y posibles tests que mencione el diff.

---

<sub>15s</sub>

### `view`

**view the file at C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\dto.**

C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\dto

<details>
<summary>8 lines</summary>

```
ErrorResponse.java
ProjectRequest.java
ProjectResponse.java
ProjectSummaryResponse.java
TaskRequest.java
TaskResponse.java
TaskStatusUpdateRequest.java
auth
```

</details>


---

<sub>17s</sub>

### `view`

**view the file at C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\dto\ProjectSummaryResponse.java.**

C:\Users\PC\Documents\taskflow-copilot-rubenaoz\src\main\java\com\taskflow\dto\ProjectSummaryResponse.java

<details>
<summary>13 lines</summary>

```
package com.taskflow.dto;

import java.util.Map;

/**
 * DTO de salida para el resumen de un proyecto.
 */
public record ProjectSummaryResponse(
        Long projectId,
        String projectName,
        int totalTasks,
        Map<String, Integer> byStatus,
        int overdue
) {
}
```

</details>


---

<sub>43s</sub>

### Copilot

Resumen de la revisión contra issues/summary.md.

| # | Gravedad | Archivo:línea | Punto checklist | Qué pasa | Qué harías |
|---|---|---:|---|---|---|
| 1 | Corregir | tests (no hay archivos nuevos) | 2,7 (Tests existentes / Tests que prueban) | El diff añade controller/service/mapper pero no incluye los tests unitarios de servicio ni los slice (@WebMvcTest) requeridos por la especificación. | Añadir: (a) Unit tests de ProjectService.resumenDe: caso con tareas en los 3 estados y una vencida; caso sin tareas. (b) @WebMvcTest de ProjectController: 200 con JSON esperado y 404 para id inexistente. |
| 2 | Sugerencia | src/main/java/com/taskflow/controller/ProjectController.java (getProjectSummary) | 4,5 (Contrato / Convenciones) | El método usa el tipo totalmente cualificado (com.taskflow.dto.ProjectSummaryResponse) mientras el resto del controller importa DTOs. No es un fallo funcional, solo inconsistencia estilística. | Importar ProjectSummaryResponse y usar el nombre simple para mantener consistencia con el resto del código. |
| 3 | Sugerencia | src/main/java/com/taskflow/mapper/ProjectMapper.java (aSummaryResponse) | 3,4 (Reglas de dominio / Contrato) | El mapper convierte correctamente TaskStatus→String iterando TaskStatus.values() y toma valores por defecto 0 — cumple la regla de "siempre las tres claves". | Ninguna acción necesaria; mantener como está. |

Casos sin test:
- Unit (ProjectService.resumenDe):
  - Proyecto con tareas en TODO, IN_PROGRESS, DONE y exactamente una tarea que t.estaVencida() -> assert total/byStatus/overdue.
  - Proyecto sin tareas -> total 0, byStatus con las tres claves en 0, overdue 0.
- Slice (@WebMvcTest ProjectController):
  - GET /projects/{id}/summary -> 200 y JSON con los 5 campos (probar con datos de la semilla, p.ej. project 2).
  - GET /projects/99/summary -> 404 con ErrorResponse uniforme.
- (Sugerido) Tests que validen los valores esperados según la tabla de la semilla para /projects/1, /2, /3.

Veredicto: CAMBIOS PEDIDOS

He leído: evidencia/dia4/summary.diff, issues/summary.md, src/main/java/com/taskflow/controller/ProjectController.java, src/main/java/com/taskflow/service/ProjectService.java, src/main/java/com/taskflow/mapper/ProjectMapper.java, src/main/java/com/taskflow/dto/ProjectSummaryResponse.java. Si se desea, aplico los cambios de estilo y creo los tests solicitados.

---

<sub>Generated by [GitHub Copilot CLI](https://github.com/features/copilot/cli)</sub>
