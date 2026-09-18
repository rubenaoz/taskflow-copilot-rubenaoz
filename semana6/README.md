# Proyecto final · Semana 6 · GitHub Copilot

**Alumno:** `Ruben Ontiveros` · **Usuario de GitHub:** `rubenaoz`

## 1. Qué construí

| | Feature | Especificación |
|---|---|---|
| [x] | `GET /reports/progress` — avance por proyecto | [`specs/progress.md`](../specs/progress.md) |

## 2. El pull request

- **URL del PR (mergeado):** `https://github.com/rubenaoz/taskflow-copilot-rubenaoz/pull/5`
- **Commit del merge en `main`:** `657f2c8 Merge pull request #5 from rubenaoz/feature/progress`
- **Comentarios de Copilot code review:** 2

## 3. Cómo lo hice

| Paso | Qué hice | Evidencia |
|---|---|---|
| Rama y spec | `git switch -c feature/progress` y copié la spec a `specs/` | `git log --oneline main..feature/progress` (antes del merge) |
| Implementación | `copilot -p "/crear-endpoint-taskflow …"` con `gpt-5-mini` | `semana6/sesion-implementacion.md` (tiene la línea `Skill "crear-endpoint-taskflow" loaded successfully`) |
| Revisión | agente `revisor` sobre `semana6/proyecto-final.diff` | `semana6/revision.md` (termina con `Veredicto: APROBADO`) |
| Tests | `mvn clean test` en verde | `Tests run: 82, Failures: 0, Errors: 0, Skipped: 0` |
| Comprobación REST | `verificar.ps1` con `casos-progress.ps1` | sección 5 de este documento |
| Code review | Copilot en el PR | la pestaña *Files changed* del PR |

## 4. Qué hizo el agente y qué corregí yo

| # | Qué hizo mal el agente (archivo) | Quién lo detectó | Cómo quedó corregido |
|---|---|---|---|
| 1 | No agregó `/reports/progress` al `@ValueSource` de rutas protegidas — `src/test/java/com/taskflow/integration/SecurityRulesTest.java` | Copilot code review | Prompt `copilot -p "Lee semana6/code-review.md y aplica esos comentarios..."`; commit `fix: comentarios de Copilot code review` |
| 2 | El slice test solo verificaba 3 de 9 campos del contrato (faltaban `projectName` y `doneTasks` en ambos elementos) — `src/test/java/com/taskflow/slice/ProgresoProyectosControllerTest.java` | Copilot code review | Mismo prompt y commit que el #1 — se ampliaron las aserciones |

**Lo que el agente hizo bien a la primera:** el DTO (`ProjectProgressResponse`), el cálculo y redondeo del porcentaje en `ProjectService` (`Math.round(raw * 10.0) / 10.0`), y el mapper — el `revisor` los marcó como conformes con la spec, dando `APROBADO` con solo 3 sugerencias de estilo no bloqueantes (nombre totalmente cualificado en vez de import, y un mock redundante en el slice test).

**Nota:** durante la implementación, el agente autocorrigió 8 líneas en `ProgresoProyectosServiceTest.java` porque los títulos de tarea que usó en los datos de prueba eran demasiado cortos para pasar la validación de `Task` (mínimo 3 caracteres) — lo detectó él mismo al correr `mvn -q test` y lo arregló antes de reportar éxito.

## 5. Comprobaciones REST

```text
Repositorio: C:\Users\PC\Documents\taskflow-copilot-rubenaoz
URL de la app: http://127.0.0.1:8080
Empaquetando con Maven (mvn -q package -DskipTests), tarda unos segundos...
App arrancando (PID 16824). Esperando a que /info responda...
App lista en 5 s.
[OK]    GET /tasks/overdue devuelve solo la tarea 7
[OK]    GET /tasks/unassigned devuelve las tareas 4 y 6
[OK]    GET /projects/1/summary
[OK]    GET /projects/2/summary
[OK]    GET /projects/3/summary
[OK]    GET /projects/99/summary responde 404
[OK]    GET /projects/1/summary sin token responde 401
[OK]    GET /reports/progress devuelve los 3 proyectos con la semilla
[OK]    El proyecto 1 se llama «Plataforma TaskFlow»
[OK]    Tras pasar la tarea 1 a DONE, el proyecto 1 queda en 2/5 = 40
[OK]    GET /reports/progress sin token responde 401
App detenida (PID 16824).
[OK]    App apagada: el puerto 8080 ya no responde
RESULTADO: 12/12 OK
```

## 6. Créditos de la semana

| Qué | AI credits |
|---|---|
| Usados en septiembre según github.com (incluye semanas anteriores si usaste Copilot antes) | `148.96` |
| Implementación con la skill (`AI Credits` del PF-2) | 7.77 (4m 53s) |
| Revisión del `revisor` (`AI Credits` del PF-3) | 2.74 (1m 54s) |
| Correcciones del PF-4 y del PF-6, si hubo (`AI Credits`) | 3.03 (2m 51s) — PF-6; PF-4 no aplicó |