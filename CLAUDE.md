# AVISOS - Advanced Visual Infrastructure Secure Operational Systems

SCADA orchestration platform with computer vision AI for predictive threat modeling. Multi-module Maven monorepo with Java services communicating over MQTT via Protobuf telemetry, plus a C++ hardware simulator used to feed realistic node vitals during demos and load tests.

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

# Build node + C++ hardware simulator images
./scripts/spawn-test-fleet.sh build

# Spawn simulator+node pairs against the running core stack
./scripts/spawn-test-fleet.sh 10

# Dry-run removal of random/load-test node rows from the SQLite DB
./scripts/purge-test-nodes.sh

# Frontend dev server (hot reload, proxies to backend)
cd avisos-controller-service/src/main/frontend && npm run dev
```

Java 25 with `--enable-preview`. Requires Mockito agent for tests (auto-copied to `target/mockito-agent/` by maven-dependency-plugin).

The React frontend is built automatically during `mvn install` via `frontend-maven-plugin` (installs Node v22, runs `npm install` + `npm run build`). Output goes to `src/main/resources/static/` and is served by Spring Boot.

## Project Structure

```
avisos-common-lib/          Protobuf definitions + generated code (telemetry.proto)
avisos-controller-service/  Central orchestration: CLI, DB, alarms, vision, notifications
avisos-node-service/        Lightweight datacenter sensor node: heartbeat, battery, telemetry
avisos-hardware-simulator/  C++17/CMake simulator exposing REST hardware readings for nodes
avisos-knowledge/           Datacenter runbooks and facility docs for future RAG enrichment
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

Lightweight datacenter sensor node with custom DIY IoC framework (no Spring Boot -- edge device footprint).

| Package | Purpose |
|---|---|
| `framework/` | Custom DIY IoC container (AppContainer, AppLifeCycle, ConfigLoader, AspectProcessor) |
| `config/` | Configuration records (AppConfig, NodeConfig, MqttConfig) |
| `hardware/` | Battery monitoring (OSHI) |
| `network/` | MQTT client (Paho) + ReactiveBufferManager |
| `service/` | HeartbeatService, NodeRuntime |

Entry point: `NodeApplication.main()`

The node can run against local OS hardware providers or a simulator-backed REST provider. In simulator mode, set `HARDWARE_PROVIDER=simulator-rest` and `HARDWARE_SIMULATOR_BASE_URL=http://<simulator-host>:5000`; the node polls simulator readings and preserves the MQTT telemetry contract consumed by the controller.

### Hardware Simulator (`avisos-hardware-simulator`)

C++17 service built with CMake. It models datacenter hardware vitals and exposes them over HTTP so `avisos-node-service` can consume realistic sensor data without needing physical hardware.

| Area | Purpose |
|---|---|
| `src/controller/` | HTTP controller for simulator endpoints such as hardware readings |
| `src/service/` | Simulation runtime and state transitions |
| `src/model/` | Hardware snapshot and node metadata models |
| `src/util/` | Supporting utility classes |

Build/runtime notes:
- Docker image is built from `hardware-simulator.Dockerfile`
- Local executable target is `avisos_hardware_simulator`
- Fleet testing is managed by `scripts/spawn-test-fleet.sh`, which starts paired simulator and node containers on the Avisos Docker network

## Code Style

- **Google Java Format** enforced by Spotless (build fails on violations)
- **C++ simulator** uses C++17 and CMake; keep it framework-light and service-oriented
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
| `mosquitto` | avisos-broker | MQTT broker (port 1883) |
| `localstack` | avisos-cloud | AWS SNS emulation (port 4567) |
| `vision-api` | avisos-VisionRequest | CodeProject.AI detection (port 32168) |

Simulator-backed nodes are not part of the base Compose stack. They are launched on demand by `scripts/spawn-test-fleet.sh`, which builds `avisos-node:latest` and `avisos-hardware-simulator:latest`, then starts paired `node-*` and `sim-*` containers.

`scripts/purge-test-nodes.sh` is an operator-only DB maintenance script for demo/load-test cleanup. It is intentionally outside controller business logic, defaults to dry-run, preserves the deterministic 20-node demo fleet by default, and requires `--yes` before deleting rows. Use `--include-demo --yes` only when deliberately resetting the canonical demo nodes. The script uses the local SQLite database through `sqlite3`.

## Environment Variables

**Controller:** `MQTT_BROKER_URL`, `MQTT_TOPIC`, `VISION_API_URL`, `DATABASE_URL`, `AVISOS_CLI_ENABLED`
**Node:** `MQTT_BROKER_URL`, `MQTT_TOPIC`, `NODE_NAME`, `NODE_TYPE`, `HARDWARE_PROVIDER`, `HARDWARE_SIMULATOR_BASE_URL`

## Future Directions

Planned roadmap items are documented in [`docs/future-directions.md`](docs/future-directions.md). When an agent is instructed to work on a future direction, the task will be referenced by its numbered item from that document:

1. ~~**Spring Boot Migration (Controller) + Custom Framework Reuse (Node)**~~ -- COMPLETED
2. ~~**Web Dashboard with Embedded CLI**~~ -- COMPLETED
3. **AI SOC Analyst Agent**
