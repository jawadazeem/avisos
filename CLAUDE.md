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

# Frontend dev server (hot reload, proxies to backend)
cd avisos-controller-service/src/main/frontend && npm run dev
```

Java 25 with `--enable-preview`. Requires Mockito agent for tests (auto-copied to `target/mockito-agent/` by maven-dependency-plugin).

The React frontend is built automatically during `mvn install` via `frontend-maven-plugin` (installs Node v22, runs `npm install` + `npm run build`). Output goes to `src/main/resources/static/` and is served by Spring Boot.

## Project Structure

```
avisos-common-lib/          Protobuf definitions + generated code (telemetry.proto)
avisos-controller-service/  Central orchestration: CLI, DB, alarms, vision, notifications
avisos-node-service/        Lightweight IoT node: heartbeat, battery, telemetry
mosquitto/                  MQTT broker config (Eclipse Mosquitto)
```

### Controller Service (`com.azeem.avisos.controller`)

Spring Boot 3.4.1 application with auto-configuration and component scanning.

| Package | Purpose |
|---|---|
| `config/` | `@ConfigurationProperties` records, `@Configuration` classes (JDBI, services, health, CLI, ingress, WebSocket) |
| `infrastructure/` | CLI (JLine REPL + BufferingCliClient), MQTT ingress, health, vision client, shutdown |
| `service/` | Business logic: alarms, nodes, threats, notifications, vision (`@Service` annotated) |
| `web/api/` | REST controllers (`/api/nodes`, `/api/alarms`, `/api/health`, `/api/system`) |
| `web/cli/` | WebSocket CLI bridge (`@MessageMapping("/cli")`) |
| `web/broadcast/` | Domain event → STOMP topic broadcaster |
| `web/event/` | Domain events (AlarmCreated, NodeHeartbeat, VisionAnalysis) |
| `repository/` | JDBI SQL object interfaces (SQLite, created via `jdbi.onDemand()` beans) |
| `model/` | Domain models (Java records) |
| `entity/` | Database entities with JDBI row mappers |
| `mapper/` | Entity <-> domain model mappers |
| `security/` | Auth with Argon2 hashing, ThreadLocal SecurityContext |
| `instrumentation/` | AOP annotations (@Timed, @ServiceAudit) |
| `src/main/frontend/` | React 19 + TypeScript + Vite SPA (SCADA dark theme, built into jar) |

Entry point: `AvisosControllerServiceApplication` (`@SpringBootApplication`, Tomcat on port 8080)

Web dashboard at `http://localhost:8080` -- React SPA with 4 pages (Dashboard, Nodes, Alarms, CLI terminal). Real-time updates via STOMP WebSocket at `/ws`. CLI is also accessible via the embedded terminal widget.

CLI REPL is optional -- enabled via `avisos.cli.enabled=true` (default `false` in web/Docker mode).

### Node Service (`com.azeem.avisos.node`)

Lightweight IoT node with custom DIY IoC framework (no Spring Boot -- edge device footprint).

| Package | Purpose |
|---|---|
| `framework/` | Custom DIY IoC container (AppContainer, AppLifeCycle, ConfigLoader, AspectProcessor) |
| `config/` | Configuration records (AppConfig, NodeConfig, MqttConfig) |
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

- **Spring Boot 3.4.1** for controller service (BOM import, not parent POM)
- **Custom DIY IoC** for node service (`framework/` package -- lightweight edge footprint)
- **Domain models are Java records** (immutable)
- **JDBI for data access** (SQL object interfaces), not JPA/Hibernate
- **SQLite** as embedded database
- **Protobuf** for node-to-controller telemetry over MQTT
- **Virtual threads** (Project Loom) for concurrent operations
- **Lombok** used in controller service (entities, builders)
- **Controller config** via `@ConfigurationProperties` records under `avisos.*` prefix
- **Node config** loaded from `application.yml` with environment variable overrides

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
| `controller` | avisos-controller | Orchestration service (port 8080) |
| `node-01` | avisos-node-01 | Sample node |
| `mosquitto` | avisos-broker | MQTT broker (port 1883) |
| `localstack` | avisos-cloud | AWS SNS emulation (port 4567) |
| `vision-api` | avisos-VisionRequest | CodeProject.AI detection (port 32168) |

## Environment Variables

**Controller:** `MQTT_BROKER_URL`, `MQTT_TOPIC`, `VISION_API_URL`, `DATABASE_URL`, `AVISOS_CLI_ENABLED`
**Node:** `MQTT_BROKER_URL`, `MQTT_TOPIC`, `NODE_NAME`, `NODE_TYPE`

## Future Directions

Planned roadmap items are documented in [`docs/future-directions.md`](docs/future-directions.md). When an agent is instructed to work on a future direction, the task will be referenced by its numbered item from that document:

1. ~~**Spring Boot Migration (Controller) + Custom Framework Reuse (Node)**~~ -- COMPLETED
2. ~~**Web Dashboard with Embedded CLI**~~ -- COMPLETED
3. **AI SOC Analyst Agent**
