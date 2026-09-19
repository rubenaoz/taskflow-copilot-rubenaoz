> 📋 **Evidencia de la Semana 6:** [EVIDENCIA-SEMANA-6.md](EVIDENCIA-SEMANA-6.md)

---

# `taskflow-api` â€” la API REST completa

El proyecto grande del curso, en su estado final (**v3.0**). A diferencia del resto del
repositorio â€”donde cada carpeta aÃ­sla **un** conceptoâ€” aquÃ­ conviven todos a la vez, que es
como se los va a encontrar en un trabajo: REST, DTOs, validaciÃ³n, JPA, seguridad con JWT,
manejo centralizado de errores, tests en tres niveles y un contenedor.

## QuÃ© resuelve

**Nodo Digital**, una agencia de software de ~15 personas, coordinaba sus proyectos entre un
grupo de WhatsApp y un Google Sheets. Cuatro dolores concretos: nadie sabÃ­a quiÃ©n tenÃ­a quÃ©;
Â«ya estÃ¡Â» no significaba nada porque nadie firmaba; el Sheets lo editaba cualquiera (un viernes
alguien borrÃ³ la pestaÃ±a de otro squad); y las fechas se perdÃ­an en el chat.

TaskFlow es la API que ordena eso: proyectos con tareas, cada tarea con un responsable, un
estado y una fecha; nadie entra sin identificarse y nadie borra el proyecto de otro.

**Esto importa para leer el cÃ³digo:** cada regla de negocio de abajo naciÃ³ de uno de esos cuatro
dolores. No son validaciones decorativas.

## Los datos con los que arranca

Con la base vacÃ­a, `DataSeeder` crea tres usuarios y tres proyectos. No son relleno: son el
reparto de la historia.

| Usuario | Password | Rol | Es dueÃ±o de | Sirve para ver |
|---|---|---|---|---|
| `ana` | `ana123` | `USER` | Plataforma TaskFlow, MigraciÃ³n Legacy | El camino feliz |
| `luis` | `luis123` | `USER` | App MÃ³vil | **El 403**: tiene proyecto propio y aun asÃ­ no puede borrar el de Ana |
| `admin` | `admin123` | `ADMIN` | â€” | La excepciÃ³n: pasa por encima de la regla de dueÃ±o |

## Los endpoints

| MÃ©todo | Ruta | Notas |
|---|---|---|
| `POST` | `/auth/register` Â· `/auth/login` | PÃºblico. El login devuelve el JWT |
| `GET` | `/auth/me` | QuiÃ©n soy, segÃºn el token |
| `GET` `POST` | `/projects` | Listar y crear |
| `GET` `PUT` `DELETE` | `/projects/{id}` | El `DELETE` solo lo puede el dueÃ±o o un `ADMIN` |
| `GET` `POST` | `/projects/{id}/tasks` | Las tareas de un proyecto. Una tarea **nace** dentro de su proyecto |
| `GET` | `/tasks` Â· `/tasks/{id}` | `/tasks?status=DONE` filtra |
| `PUT` `DELETE` | `/tasks/{id}` | Actualizar completa / borrar |
| `PATCH` | `/tasks/{id}/status` | Cambiar solo el estado |
| `GET` | `/info` | PÃºblico: nombre y versiÃ³n. Es el smoke test de un despliegue |

DocumentaciÃ³n viva en `http://localhost:8080/swagger-ui/index.html`.
ColecciÃ³n de Postman lista para importar en [`postman/`](postman/).

## Las reglas, y el cÃ³digo HTTP que verÃ­as al romperlas

La tabla que vale la pena saberse: **cada regla vive en un lugar concreto**, y ese lugar es una
decisiÃ³n de diseÃ±o, no un accidente.

| Regla | DÃ³nde vive | Rompela y ves |
|---|---|---|
| Una tarea no existe sin proyecto | Constructor de `Task` + el `projectId` sale del path | `400` |
| `title` entre 3 y 120 caracteres | Constructor de `Task` â†’ `TaskValidationException` | `400` |
| `dueDate` no puede ser pasada **al crear** | Factory `Task.crear` â€” el constructor sÃ­ rehidrata tareas vencidas, o `estaVencida()` no tendrÃ­a sentido | `400` |
| No se pasa a `DONE` sin responsable | `Task.setStatus` â€” la regla vive en el **dominio**; `TaskService` la traduce | `422` |
| Solo el dueÃ±o o `ADMIN` borra un proyecto | `@PreAuthorize` + el bean `ProjectSecurity` | `403` |
| Nadie entra sin identificarse | Filtro JWT | `401` |
| Una tarea que no existe se dice claro | `TaskNotFoundException` â†’ el `@RestControllerAdvice` | `404` |

Ojo a la distinciÃ³n **401 vs 403**: sin token es `401` (Â«no sÃ© quiÃ©n eresÂ»); con token vÃ¡lido
pero sin permiso es `403` (Â«sÃ© quiÃ©n eres, y no puedesÂ»). Que salgan los dos cÃ³digos correctos
depende de que `SecurityConfig` declare **ambos**, `authenticationEntryPoint` y
`accessDeniedHandler` â€” con uno solo, todo cae en `401` y la diferencia se pierde.

## CÃ³mo levantarlo â€” lo Ãºnico que necesitas es Docker

Basta con **Docker Desktop actual** (o Docker Engine con el plugin Compose v2). No instalas nada
mÃ¡s: **ni JDK, ni Maven, ni Postgres, ni un IDE.** El `Dockerfile` es multi-etapa y trae lo suyo:

| Etapa | Imagen | QuÃ© aporta |
|---|---|---|
| `build` | `maven:3.9-eclipse-temurin-21` | Compila y empaqueta **dentro** del contenedor |
| runtime | `eclipse-temurin:21-jre` | Solo el JRE 21 y el jar â€” ni Maven ni el JDK viajan a la imagen final |

Y no hay nada que configurar: el `docker-compose.yml` trae valores por defecto para usuario,
contraseÃ±a, base y secret. Se arranca tal cual sale del clon.

### Antes de empezar, una comprobaciÃ³n de 5 segundos

```bash
docker compose version      # tiene que responder v2.x o superior
```

Si ese comando no existe y en tu mÃ¡quina solo hay `docker-compose` (con guion, el antiguo), hay
que actualizar Docker Desktop antes de seguir. **Este `docker-compose.yml` estÃ¡ escrito en formato
Compose v2** â€”por eso no declara `version:`â€” y el Compose v1 no sabe leerlo: lo interpreta como el
formato viejo y falla con errores de opciones no soportadas que no dicen cuÃ¡l es la causa real.
Cualquier Docker Desktop de los Ãºltimos aÃ±os ya trae la v2.

### Los tres pasos

```bash
git clone https://github.com/cursosmrugerio/academyMty.git
cd academyMty/taskflow-api
docker compose up --build
```

Eso es todo. En Windows funciona igual, en PowerShell o en `cmd`.

### QuÃ© acabas de levantar

| Contenedor | QuÃ© es | DÃ³nde queda |
|---|---|---|
| `db` | PostgreSQL 16 con su volumen propio | `localhost:5432` (solo para inspeccionar con psql o DBeaver) |
| `api` | La API con el perfil `docker`, contra ese Postgres | `http://localhost:8080` |

La API arranca con la base sembrada: los usuarios `ana` / `ana123`, `luis` / `luis123` y
`admin` / `admin123`, y los tres proyectos de la agencia.

El `depends_on` espera al **healthcheck** de la base, no solo a que el contenedor arranque. La
diferencia entre eso y un `depends_on` pelado es una API que se cae al iniciar porque Postgres
todavÃ­a no aceptaba conexiones.

### Comprobar que estÃ¡ vivo

```bash
curl http://localhost:8080/info
# {"app":"taskflow-api","version":"3.0.0"}
```

O abre `http://localhost:8080/swagger-ui/index.html`, entra con `ana` / `ana123` en
`POST /auth/login`, y pega el token en el botÃ³n **Authorize**.

### Pararlo

```bash
docker compose down       # para los contenedores, conserva los datos
docker compose down -v    # ademÃ¡s borra el volumen: la prÃ³xima vez arranca de cero
```

### Lo Ãºnico que puede salirte mal

| SÃ­ntoma | QuÃ© pasa |
|---|---|
| `Cannot connect to the Docker daemon` | Docker no estÃ¡ corriendo. Abre Docker Desktop y espera a que estÃ© en verde |
| Errores de sintaxis u Â«opciÃ³n no soportadaÂ» al leer el `docker-compose.yml` | EstÃ¡s con Compose **v1** (`docker-compose`, con guion). Comprueba con `docker compose version` y actualiza Docker Desktop |
| `port is already allocated` | Algo mÃ¡s ocupa el **8080** o el **5432**. LibÃ©ralo, o cambia el lado izquierdo del `ports:` en el compose (`"8081:8080"`) |
| La primera vez tarda mucho | Normal: la etapa de build descarga todo el Ã¡rbol de dependencias de Spring dentro del contenedor, y necesita internet. Mientras no toques el `pom.xml`, esa capa queda en cachÃ© y las siguientes son segundos |

### Si quieres cambiar la configuraciÃ³n (opcional)

Los valores por defecto son de desarrollo y estÃ¡n a la vista en el compose. Para cambiarlos, sin
tocar ningÃºn archivo versionado:

```bash
cp .env.example .env      # copy .env.example .env  en Windows
```

y edita ahÃ­ `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB` y `JWT_SECRET`. El `.env` no se
commitea nunca. **Fuera de tu mÃ¡quina, el `JWT_SECRET` se cambia sÃ­ o sÃ­**: el que viene por
defecto es pÃºblico, estÃ¡ en este repositorio, y con Ã©l cualquiera puede firmar un token vÃ¡lido.

### Dos cosas que no tienes que revisar

**Intel o ARM, da igual.** Las tres imÃ¡genes base publican `amd64` y `arm64`, y como el compose
construye **en cada mÃ¡quina**, cada una genera la suya nativa. Un Mac con Apple Silicon y un PC
con Windows corren el mismo comando sin tocar una lÃ­nea.

**Los finales de lÃ­nea, tampoco.** Git para Windows los convierte al clonar, y un `\r` colado en
el `.env` entra *dentro* del valor: `JWT_SECRET=abc\r` firma con `abc\r`, la API arranca tan
tranquila y todo login devuelve `401` sin decir por quÃ©. El `.gitattributes` de esta carpeta
fuerza LF, asÃ­ que no puede pasar.

## Otras formas de correrlo, si vas a tocar el cÃ³digo

Estas sÃ­ necesitan JDK 21 y Maven en tu mÃ¡quina, y no usan Postgres: el perfil por defecto
levanta una **H2 en archivo** (`data/taskflow.mv.db`), asÃ­ que los datos sobreviven al reinicio.
Consola SQL en `/h2-console`.

| Desde | CÃ³mo |
|---|---|
| Eclipse | Importar como proyecto Maven y ejecutar `TaskflowApiApplication` como Java Application |
| Terminal | `mvn spring-boot:run` |

## Los tests

| Comando | QuÃ© corre |
|---|---|
| `mvn test` | **67 tests**: unitarios, slices (`@WebMvcTest`, `@DataJpaTest`) e integraciÃ³n (`@SpringBootTest`) |
| `mvn verify` | Los 67 **+ el gate de cobertura**: falla por debajo del 70% de lÃ­neas. Hoy va en 86.7% |
| `mvn test -Ddocker.tests=true` | **70**: aÃ±ade `TaskRepositoryPostgresIT`, el mismo test de repositorio contra un Postgres 16 real y efÃ­mero (Testcontainers). Requiere Docker levantado |

Informe de cobertura tras `mvn verify`: `target/site/jacoco/index.html`.

### Dos trampas que ya estÃ¡n resueltas aquÃ­

**JaCoCo vive en un perfil, no en `<build>`.** Instrumenta con un `-javaagent` que inyecta en
`${argLine}`, y m2e no ejecuta `prepare-agent`: dentro de Eclipse la variable se queda sin
resolver y **`Run As > JUnit Test` revienta**. El perfil `cobertura` se activa con
`!m2e.version`, o sea en la terminal y no en Eclipse. Es el mismo patrÃ³n que usa el proyecto
[`mockito`](../mockito/) de este repositorio, por la misma razÃ³n.

**El test de Testcontainers fija `api.version=1.41`.** Sin eso, el cliente `docker-java` que
arrastra Spring Boot 3.5 negocia por debajo de la API mÃ­nima de Docker moderno (Min API 1.40) y
falla con Â«*Could not find a valid Docker environment*Â» â€” un mensaje que no menciona versiones
y te manda a revisar si el demonio estÃ¡ encendido, que no era el problema.

## QuÃ© NO estÃ¡ aquÃ­, a propÃ³sito

- **Nada de AWS**: ni despliegue en EC2/RDS, ni las evidencias, ni el plan sin cuenta de nube.
- **Nada de CI/CD**: el pipeline de GitHub Actions y la publicaciÃ³n de la imagen no viajaron.
- **Ninguna feature nueva**: comentarios en tareas, etiquetas y notificaciones son el backlog
  del cliente, no parte de v3.0.
- **Sin frontend**: TaskFlow es una API. Si algÃºn dÃ­a se le sirve una UI estÃ¡tica desde
  `src/main/resources/static/`, hay que abrirla en `SecurityConfig` â€” con el `anyRequest()
  .authenticated()` actual, hasta el CSS responde `401`.

