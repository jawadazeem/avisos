# AVISOS
***A***dvanced ***V***isual ***I***nfrastructure ***S***ecure ***O***perational ***S***ystems

**Author:** Jawad Azeem
**Live:** [avisos.jawadazeem.com](https://avisos.jawadazeem.com)

AVISOS is a SCADA orchestration platform that secures and monitors high-reliability environments using computer vision AI for real-time threat detection. Edge nodes publish telemetry over MQTT, the central controller evaluates frames through a vision AI pipeline, and operators interact through a React web dashboard with live WebSocket updates and an embedded CLI terminal.

### Architecture

![Architecture](images/architecture-diagram.svg)

### Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 25 (preview), Spring Boot 3.4.1, Virtual Threads |
| Frontend | React 19, TypeScript, Vite, xterm.js |
| Messaging | Eclipse Mosquitto (MQTT), Protobuf telemetry |
| Real-time | STOMP over WebSocket (SockJS) |
| Database | SQLite + SQLCipher (encrypted), JDBI |
| Vision AI | CodeProject.AI object detection |
| Hardware Simulation | C++17, CMake, cpp-httplib, nlohmann/json |
| Cloud | AWS SNS via LocalStack |
| Security | Argon2id password hashing, encrypted DB |
| Build | Maven multi-module, frontend-maven-plugin (Node 22) |
| Deploy | Docker Compose + on-demand simulator/node fleet containers |

### Module Structure

```
avisos-common-lib/          Protobuf definitions + generated code (telemetry.proto)
avisos-controller-service/  Central orchestration: REST API, dashboard, alarms, vision, CLI
avisos-node-service/        Lightweight datacenter sensor node: heartbeat, battery, telemetry
avisos-hardware-simulator/  C++ hardware simulator: REST readings for node-service polling
avisos-knowledge/           Datacenter runbooks and facility docs for future RAG enrichment
mosquitto/                  MQTT broker configuration (Eclipse Mosquitto)
```

### Quick Start

```bash
# Full stack (controller + node + broker + vision + cloud)
docker compose up --build

# Frontend dev server with hot reload (proxies to backend on :8080)
cd avisos-controller-service/src/main/frontend && npm run dev

# Build all modules
mvn clean install

# Build node + C++ hardware simulator images for fleet testing
./scripts/spawn-test-fleet.sh build

# Spawn 10 simulator+node pairs against the running core stack
./scripts/spawn-test-fleet.sh 10
```

Dashboard at `http://localhost:8080` -- pages for system overview, node monitoring, alarm management, and an embedded CLI terminal. All updates stream in real-time over WebSocket.

The C++ hardware simulator runs as a standalone REST process and exposes hardware readings for the Java node service. In simulator mode, each node polls its paired simulator via `HARDWARE_SIMULATOR_BASE_URL`, then publishes the existing MQTT telemetry contract to the controller.

### API

| Endpoint | Description |
|---|---|
| `GET /api/nodes` | List registered edge nodes |
| `GET /api/alarms` | Active alarms with severity |
| `GET /api/health` | System health report |
| `GET /api/system/stats` | JVM runtime statistics |
| `WS /ws` | STOMP WebSocket (topics: `/topic/nodes`, `/topic/alarms`, `/topic/vision`, `/topic/cli`) |

### Roadmap

See [`docs/future-directions.md`](docs/future-directions.md) for planned features including the AI SOC Analyst Agent.

### License

Apache 2.0
