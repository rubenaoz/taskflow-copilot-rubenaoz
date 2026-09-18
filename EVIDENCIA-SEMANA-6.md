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

