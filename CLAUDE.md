# AVISOS - Advanced Visual Infrastructure Secure Operational Systems

SCADA orchestration platform with computer vision AI for predictive threat modeling. Multi-module Maven monorepo with two services communicating over MQTT via Protobuf telemetry.

## Build & Test

```bash
# Build all modules (runs spotless:check in validate phase)
mvn clean install

# Run tests only
mvn test

# Check code formatting
mvn spotless:check

# Auto-fix code formatting
mvn spotless:apply

# Run the full stack via Docker
docker compose up --build
```

Java 25 with `--enable-preview`. Requires Mockito agent for tests (auto-copied to `target/mockito-agent/` by maven-dependency-plugin).

## Project Structure

```
avisos-common-lib/          Protobuf definitions + generated code (telemetry.proto)
avisos-controller-service/  Central orchestration: CLI, DB, alarms, vision, notifications
avisos-node-service/        Lightweight IoT node: heartbeat, battery, telemetry
mosquitto/                  MQTT broker config (Eclipse Mosquitto)
```

### Controller Service (`com.azeem.avisos.controller`)

| Package | Purpose |
|---|---|
| `framework/` | Custom DIY IoC container (AppContainer, AppLifeCycle) |
| `infrastructure/` | CLI (JLine REPL), MQTT ingress, health, vision client, shutdown |
| `service/` | Business logic: alarms, nodes, threats, notifications, vision |
| `repository/` | JDBI SQL object interfaces (SQLite) |
| `model/` | Domain models (Java records) |
| `entity/` | Database entities with JDBI row mappers |
| `mapper/` | Entity <-> domain model mappers |
| `security/` | Auth with Argon2 hashing, ThreadLocal SecurityContext |
| `instrumentation/` | AOP annotations (@Timed, @ServiceAudit) |
| `config/` | YAML configuration POJOs |

Entry point: `AvisosControllerServiceApplication` (instance main, Java 25 preview)

### Node Service (`com.azeem.avisos.node`)

| Package | Purpose |
|---|---|
| `config/` | ConfigLoader with env var overrides |
| `hardware/` | Battery monitoring (OSHI) |
| `network/` | MQTT client (Paho) + ReactiveBufferManager |
| `service/` | HeartbeatService, NodeRuntime |

Entry point: `NodeApplication.main()`

## Code Style

- **Google Java Format** enforced by Spotless (build fails on violations)
- Run `mvn spotless:apply` before committing
- `.editorconfig`: UTF-8, LF line endings, 4-space indent, trim trailing whitespace
- Unused imports are automatically removed by Spotless

## Key Conventions

- **No Spring Boot** -- custom IoC container in `framework/` package (learning project)
- **Domain models are Java records** (immutable)
- **JDBI for data access** (SQL object interfaces), not JPA/Hibernate
- **SQLite** as embedded database
- **Protobuf** for node-to-controller telemetry over MQTT
- **Virtual threads** (Project Loom) for concurrent operations
- **Lombok** used in controller service (entities, builders)
- **Config** loaded from `application.yml` with environment variable overrides

## Testing

- JUnit 5 (Jupiter 6.1.0-RC1) + Mockito 5.23
- `@ExtendWith(MockitoExtension.class)` pattern with `@Mock` / `@InjectMocks`
- Tests mirror main source layout under `src/test/java/`
- Mockito agent required: configured via Surefire's `-javaagent` arg

## Git Conventions

Conventional Commits format:
- `feat:` / `feat(scope):` -- new features
- `fix:` -- bug fixes
- `refactor:` -- restructuring
- `style:` -- formatting
- `test:` -- adding/updating tests

## Docker Compose Services

| Service | Container | Purpose |
|---|---|---|
| `controller` | avisos-controller | Orchestration service |
| `node-01` | avisos-node-01 | Sample node |
| `mosquitto` | avisos-broker | MQTT broker (port 1883) |
| `localstack` | avisos-cloud | AWS SNS emulation (port 4567) |
| `vision-api` | avisos-VisionRequest | CodeProject.AI detection (port 32168) |

## Environment Variables

**Controller:** `MQTT_BROKER_URL`, `MQTT_TOPIC`, `VISION_API_URL`, `DATABASE_URL`
**Node:** `MQTT_BROKER_URL`, `MQTT_TOPIC`, `NODE_NAME`, `NODE_TYPE`

## Future Directions

Planned roadmap items are documented in [`docs/future-directions.md`](docs/future-directions.md). When an agent is instructed to work on a future direction, the task will be referenced by its numbered item from that document:

1. **Spring Boot Migration (Controller) + Custom Framework Reuse (Node)**
2. **Web Dashboard with Embedded CLI**
3. **AI SOC Analyst Agent**
