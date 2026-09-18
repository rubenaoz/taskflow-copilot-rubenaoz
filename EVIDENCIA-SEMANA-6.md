## Día 1 · La CLI

- **Qué construí:** instalé la CLI de GitHub Copilot, copié `taskflow-api` a mi repo (`taskflow-copilot-rubenaoz`), y usé el agente para escribir `.github/copilot-instructions.md` y `docs/ARQUITECTURA.md`. Lo que el agente afirmó sobre el repo no lo di por bueno: lo comprobé con comandos que no dependen de él.
- **Dónde está:** [`.github/copilot-instructions.md`](.github/copilot-instructions.md) · [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md)
- **Cómo se comprueba:**
  - Suite completa en verde antes de tocar nada: **67 tests, 0 Failures, 0 Errors** (`mvn -q clean test`).

    ![Suite de 67 tests en verde](evidencia/dia1/suite-67-tests.png)

  - Le pregunté al agente qué endpoint devuelve las tareas vencidas; contestó que **no existe ninguno dedicado**. Lo comprobé listando los 16 endpoints reales del controller — ninguno es de vencidas, confirmando que no inventó uno:

    ![Los 16 endpoints reales del proyecto](evidencia/dia1/endpoints-reales.png)

  - Probé los tres tipos de permiso: aprobé `mvn -q test` (67/67 verde), **negué** un borrado de `target/` (el agente confirma "The user rejected this tool call"), y deshice una edición del `README.md` con `/rewind` → `git status` quedó limpio (`nothing to commit, working tree clean`).

    ![Permiso negado: borrado de target rechazado](evidencia/dia1/permiso-negado.png)

  - `docs/ARQUITECTURA.md` lo verifiqué con `verificar-arquitectura.ps1`, que no usa IA: busca cada clase, método y endpoint que el documento cita y confirma si existe de verdad en el código. La primera corrida marcó 9 `NO EXISTE`; después de corregir con el agente (dándole la evidencia exacta, no solo "arréglalo"), la segunda corrida cerró en **0 NO EXISTE** ([`evidencia/dia1/verificador.txt`](evidencia/dia1/verificador.txt)).

    ![Verificador de arquitectura en 0 NO EXISTE](evidencia/dia1/verificador-0-no-existe.png)

  - El archivo de instrucciones quedó publicado y visible en GitHub, no solo en mi laptop:

    ![.github/copilot-instructions.md visible en GitHub](evidencia/dia1/instructions-en-github.png)

  - Evidencia adicional en texto: [`copilot-version.txt`](evidencia/dia1/copilot-version.txt) · [`usage.txt`](evidencia/dia1/usage.txt) · [`uso-integrador.json`](evidencia/dia1/uso-integrador.json) · [`verificador.txt`](evidencia/dia1/verificador.txt)

- **Qué no salió:** en la primera corrida de `verificar-arquitectura.ps1`, el documento generado por el agente citaba una clase que no existía en el proyecto (el verificador la marcó como `NO EXISTE`). Se lo señalé al agente pasándole la línea exacta del reporte del verificador, y corrigió el documento sin tocar nada más; la segunda corrida cerró en 0.

## Día 2 · Especificar, implementar y revisar

- **Qué construí:** `GET /tasks/overdue` y `GET /tasks/unassigned`, cada uno a partir de una spec escrita antes del prompt (`specs/overdue.md`, `specs/unassigned.md`), no de una instrucción de una línea. Para `unassigned` usé `/plan` para revisar el plan del agente antes de dejarlo tocar código. Los dos se fusionaron a `main` por un PR con revisión de Copilot.
- **Dónde está:** [`specs/overdue.md`](specs/overdue.md) · [`specs/unassigned.md`](specs/unassigned.md) · PR [#1](https://github.com/rubenaoz/taskflow-copilot-rubenaoz/pull/1)
- **Cómo se comprueba:**
  - El primer intento del agente en `overdue` **falló el checklist**: creó 11 archivos cuando la spec solo pedía 4 — clases placeholder como `DataSeeder.java`, `SecurityConfig.java`, con comentarios que admitían "si existe" (bandera de que el agente no verificó lo que afirmaba).

    ![Primer intento con archivos fuera de alcance](evidencia/dia2/intento1-archivos-de-mas.png)

  - Descarté ese intento (`git reset HEAD~1`) y repetí. El segundo intento pasó el checklist completo de 6 puntos: 0 líneas borradas de tests existentes, comentarios verificados uno por uno, y una prueba de mutación (quitar `.sorted(TaskOrders.POR_FECHA)` a mano) que rompió los tests correctamente (`BUILD FAILURE`, confirmando que sí hay un test vigilando el orden) — no me creí que "la suite pasa" significara que el cambio estaba bien. El mismo patrón se repitió limpio en `unassigned` a la primera.

    ![Checklist completo: mutación en BUILD FAILURE y suite en verde](evidencia/dia2/checklist-mutacion-failure.png)

  - Para `unassigned` pedí `/plan` antes de implementar. El primer plan generado no especificaba los casos de prueba concretos que pedía la spec — lo rechacé con `4. Suggest changes` señalando exactamente qué faltaba, y el plan corregido sí los incluyó.

    ![Plan corregido con los casos de prueba exactos](evidencia/dia2/plan-corregido.png)

  - Planté un bug a propósito (`isBefore` → `isAfter` en `Task.estaVencida()`) en una rama descartable. La suite lo detectó (2 tests en rojo, `BUILD FAILURE`). Le pedí al agente "los tests fallan, haz que pasen" sin decirle dónde está el bug — corrigió el código de producción (`Task.java +1 -1`), no los tests.

    ![El agente corrige el bug en código de producción, no en tests](evidencia/dia2/bug-corregido-en-produccion.png)

  - Copilot code review en el PR dejó 2 comentarios. Apliqué el que tenía razón (fixtures sin cubrir tareas `DONE` sin responsable) y rechacé el que contradecía la spec (probar el orden en el slice, cuando la spec dice explícitamente que eso se prueba en el unit).

    ![Comentario de Copilot rechazado con motivo](evidencia/dia2/copilot-review-rechazado.png)

  - La prueba final fue la app real corriendo, no mocks: `overdue: 7`, `unassigned: 4, 6`, `sin token: 401` — exactamente lo esperado con la semilla.

    ![La app arrancada confirma los resultados exactos](evidencia/dia2/app-arrancada-comprobacion.png)

  - Evidencia adicional en texto: [`checklist-overdue.txt`](evidencia/dia2/checklist-overdue.txt) · [`comprobacion.txt`](evidencia/dia2/comprobacion.txt) · [`pr.txt`](evidencia/dia2/pr.txt) · [`suite-main.txt`](evidencia/dia2/suite-main.txt) · [`usage.txt`](evidencia/dia2/usage.txt)

- **Qué no salió:** el primer intento de `overdue` se descartó completo por crear archivos fuera del alcance de la spec (arriba). Además, mergeé el PR **antes** de pedir la revisión de Copilot (el orden correcto es al revés) — no afectó el resultado porque atendí los comentarios igual después del merge, pero el flujo quedó invertido respecto a lo que pedía la guía.
