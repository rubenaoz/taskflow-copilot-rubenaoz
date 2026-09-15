# Copilot instructions — taskflow-api

Purpose
- Short guide for AI assistants (Copilot) to understand build/test commands, architecture, and repository-specific conventions.
- Source-of-truth: README.md — consult it for domain rules and runnable examples.

Build, run, test, and (lint) commands
- Build (compile & package): mvn package
- Run locally (dev profile, H2 file DB): mvn spring-boot:run
- Run inside Docker (no JDK/Maven required): docker compose up --build
- Stop containers: docker compose down
- Remove volumes (clean DB): docker compose down -v

Tests
- Full unit/slice/integration suite: mvn test  (≈67 tests)
- Coverage gate + report: mvn verify  (activates JaCoCo profile; fails if <70%)
- Testcontainers integration (real Postgres ITs): mvn test -Ddocker.tests=true
  - Requires Docker daemon and will include *IT.java tests. The surefire config also forces api.version=1.41.
- Run a single test class: mvn -Dtest=ClassName test
- Run a single test method: mvn -Dtest=ClassName#methodName test
- Coverage report: target/site/jacoco/index.html (after mvn verify)

Notes about linting
- There is no project-level linter or Checkstyle/SpotBugs plugin configured in the POM. Use the Maven build and test suite as the primary quality gate. Add linter config if required.

High-level architecture (big picture)
- Spring Boot 3.5 application (Java 21).
- Typical layered structure:
  - Web layer: @RestController classes exposing endpoints (/auth, /projects, /tasks, /info). Swagger UI at /swagger-ui/index.html.
  - DTOs & Validation: DTOs carry request/response shapes; validation uses jakarta.validation (spring-boot-starter-validation).
  - Service layer: business orchestration and transaction boundaries.
  - Domain model: Entities (Task, Project, User) hold domain rules (constructors, setStatus, etc.). Many validation rules live inside domain classes, not only in controllers.
  - Persistence: Spring Data JPA repositories, H2 (dev) or PostgreSQL (docker profile/runtime).
  - Security: JWT-based stateless security (spring-boot-starter-security + custom SecurityConfig, JWT filter). Both authenticationEntryPoint and accessDeniedHandler are configured to preserve correct 401 vs 403 semantics.
  - Error handling: Global @RestControllerAdvice maps domain exceptions (e.g., TaskNotFoundException, TaskValidationException) to HTTP status codes.
  - Data seeding: DataSeeder pre-populates users and example projects for local/dev runs.
  - Infra: Docker multi-stage Dockerfile for build/runtime and docker-compose.yml (Compose v2) that spins Postgres + API with healthchecks.

Key repository conventions and important patterns
- Business rules live in the domain, not only in services or controllers. Expect validation exceptions to be thrown from constructors or domain methods (e.g., Task.crear, Task.setStatus).
- HTTP codes mapping: the project intentionally distinguishes 401 (no token) vs 403 (no permission). SecurityConfig must expose both authenticationEntryPoint and accessDeniedHandler for tests and correctness.
- Tests composition:
  - Unit tests and slices (@WebMvcTest, @DataJpaTest) use H2 in-memory/file.
  - A small set of Postgres ITs (disabled by default) run via Testcontainers when -Ddocker.tests=true is provided.
- Coverage gate:
  - JaCoCo is configured under the `cobertura` profile. mvn test does not run the coverage gate; use mvn verify to generate the report and enforce the 70% line coverage minimum.
  - The JaCoCo profile intentionally avoids running inside IDE junit runs (m2e issue). Use the terminal to run coverage.
- Maven profiles:
  - Default: H2 (dev). Use docker-compose or the `docker` runtime profile to target Postgres.
  - docker-it profile flips Surefire includes and sets api.version for Testcontainers.
- Docker compose: Use the v2 CLI (docker compose ...), not the legacy docker-compose (with hyphen).
- Testcontainers quirks: the project fixes api.version=1.41 to avoid Docker Java negotiation issues on modern Docker engines.
- Seeded accounts for manual testing: `ana`/`ana123` (USER), `luis`/`luis123` (USER), `admin`/`admin123` (ADMIN).
- Postman collection: postman/ directory contains an importable collection useful for manual API exploration.

Files and entry points to inspect for tasks (high-value files)
- pom.xml (profiles, test/jacoco configuration)
- src/main/java/.../security/SecurityConfig (JWT setup, handlers)
- src/main/java/.../domain/Task (domain rules and validation)
- src/main/java/.../seed/DataSeeder (pre-seeded users/projects)
- src/main/java/.../advice/RestExceptionHandler or similar (@RestControllerAdvice)
- Dockerfile and docker-compose.yml
- postman/ collection

Change guidance for Copilot suggestions
- Prefer making minimal, surgical edits: business rules are deliberate and tested; moving validation out of the domain or changing exception types may break tests.
- When adding or modifying tests that affect coverage, run mvn verify locally to ensure the JaCoCo gate still passes.
- If introducing Testcontainers tests, document the Docker requirement and set docker.tests=true; ensure api.version remains compatible.

References
- README.md: canonical explanation of domain rules, examples and quickstart.
- pom.xml: build/test/profile details and JaCoCo configuration.

If anything above should be expanded (more commands, specific file pointers, or language localization), say which area to expand.
