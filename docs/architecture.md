# AVISOS Architecture Guide

> **Advanced Visual Infrastructure Secure Operational Systems**
>
> A SCADA orchestration platform with computer vision AI for predictive threat modeling.

This document is a developer-oriented walkthrough of how the system works end-to-end. It is designed to get a new contributor (or a returning one) up to speed without reading every source file.

---

## Table of Contents

- [AVISOS Architecture Guide](#avisos-architecture-guide)
  - [Table of Contents](#table-of-contents)
  - [System Overview](#system-overview)
  - [Module Breakdown](#module-breakdown)
    - [`avisos-common-lib`](#avisos-common-lib)
    - [`avisos-node-service`](#avisos-node-service)
    - [`avisos-controller-service`](#avisos-controller-service)
  - [Runtime Flows](#runtime-flows)
    - [1. Node Startup and Heartbeat](#1-node-startup-and-heartbeat)
    - [2. Telemetry Ingress and Threat Detection](#2-telemetry-ingress-and-threat-detection)
    - [3. Controller Startup and CLI](#3-controller-startup-and-cli)
    - [4. Health Monitoring](#4-health-monitoring)
  - [Data Model](#data-model)
    - [SQLite Tables](#sqlite-tables)
    - [Domain Models](#domain-models)
  - [Concurrency Model](#concurrency-model)
  - [Security Model](#security-model)
  - [Configuration](#configuration)
    - [Controller Service](#controller-service)
    - [Node Service](#node-service)
  - [Infrastructure Services (Docker Compose)](#infrastructure-services-docker-compose)
  - [Key Design Decisions](#key-design-decisions)
    - [Spring Boot for Controller, Custom IoC for Node](#spring-boot-for-controller-custom-ioc-for-node)
    - [JDBI over JPA/Hibernate](#jdbi-over-jpahibernate)
    - [MQTT over HTTP/gRPC for telemetry](#mqtt-over-httpgrpc-for-telemetry)
    - [SQLite with SQLCipher](#sqlite-with-sqlcipher)
    - [Virtual threads everywhere](#virtual-threads-everywhere)

---

## System Overview

AVISOS is a monorepo containing two Java microservices that communicate over MQTT using Protobuf/JSON telemetry:

```
┌─────────────────────────────────────────────────────────────────┐
│                        MQTT Broker                              │
│                    (Eclipse Mosquitto)                          │
│                     avisos/telemetry                            │
└───────────┬─────────────────────────────────┬───────────────────┘
            │  publish                        │  subscribe
            │                                 │
┌───────────┴───────────┐       ┌─────────────┴─────────────────┐
│     Node Service      │       │      Controller Service       │
│                       │       │                               │
│  - Battery monitor    │       │  - Telemetry ingress          │
│  - Heartbeat loop     │       │  - Vision AI analysis         │
│  - MQTT publisher     │       │  - Alarm management           │
│  - Watchdog           │       │  - Node tracking              │
│                       │       │  - REST API + WebSocket       │
│                       │       │  - React web dashboard        │
│                       │       │  - CLI (JLine REPL / WebSocket)│
│                       │       │  - SQLite persistence         │
│                       │       │  - Health monitoring          │
└───────────────────────┘       └───────┬───────┬───────┬───────┘
                                        │       │       │
                              ┌─────────┴──┐ ┌──┴───┐ ┌─┴──────────────┐
                              │  SQLite DB │ │ Web  │ │ CodeProject.AI │
                              │ (encrypted)│ │Browser│ │  Vision API    │
                              └────────────┘ └──────┘ └────────────────┘
```

One controller instance manages many node instances. Each node represents a physical data acquisition device deployed in the field.

---

## Module Breakdown

### `avisos-common-lib`

Shared Protobuf definitions (`telemetry.proto`) and generated Java classes. Both services depend on this module for message serialization contracts.

### `avisos-node-service`

A lightweight edge service — one instance per physical device. Uses a custom DIY IoC framework (`framework/` package) instead of Spring Boot to keep the deployment footprint small for edge hardware.

Responsibilities:

- **Heartbeat generation** — sends telemetry packets every 30 seconds with battery level, node metadata, and simulated sensor payload.
- **Battery monitoring** — queries hardware via OSHI; warns if below 15%.
- **MQTT connectivity** — Eclipse Paho client with auto-reconnect and exponential backoff (1s to 60s).
- **Reactive buffering** — bounded in-memory queue (5000 items) with backpressure for unreliable networks. (Implemented but not yet wired into the runtime — see `NodeRuntime` TODO.)

Bootstrap is managed by `AppLifeCycle`, which creates an `AppContainer` (HashMap-based bean registry), runs `AspectProcessor` (annotation scanning scaffold), and wires all dependencies via constructor injection.

The node runs three concurrent virtual-thread loops managed by `NodeRuntime`:

1. **Connection supervisor** — connects to the broker, retries with backoff on failure.
2. **Heartbeat loop** — periodic telemetry publish.
3. **Watchdog** — monitors battery and connection state.

### `avisos-controller-service`

The central orchestration service, built on **Spring Boot 3.4.1**. Uses auto-configuration, component scanning (`@Service`, `@Component`), and `@ConfigurationProperties` records for type-safe configuration binding under the `avisos.*` prefix.

Responsibilities:

- **Telemetry ingress** — subscribes to MQTT, deserializes JSON payloads, and routes packets through the analysis pipeline.
- **Vision AI** — forwards image data to CodeProject.AI for object detection, then evaluates detected labels against configured threat keywords.
- **Alarm management** — persists alarms with severity (NONE/WARNING/CRITICAL) when threats are detected.
- **Node lifecycle** — tracks node heartbeats in an in-memory cache with flood protection, detects stale nodes via scheduled cleanup.
- **Web dashboard** — React 19 SPA with SCADA dark-mode theme served from embedded Tomcat. Four pages: Dashboard (system overview), Nodes, Alarms, and CLI terminal. Built with Vite and TypeScript, compiled into the jar via `frontend-maven-plugin`.
- **REST API** — `@RestController` endpoints under `/api/` for nodes, alarms, health, and system stats. `SpaForwardingController` delegates non-API paths to `index.html` for React Router.
- **WebSocket** — STOMP over SockJS at `/ws`. Domain events (`ApplicationEventPublisher`) from the telemetry pipeline are broadcast to STOMP topics (`/topic/alarms`, `/topic/nodes`, `/topic/vision`) via `DashboardEventBroadcaster`. The embedded CLI is bridged via `@MessageMapping("/cli")` with a `BufferingCliClient` that captures command output.
- **Web server** — embedded Tomcat on port 8080 with Spring Actuator endpoints (`/actuator/health`, `/actuator/info`, `/actuator/metrics`).
- **CLI** — optional JLine-based REPL (enabled via `avisos.cli.enabled=true`, disabled by default in web/Docker mode) with authentication (Argon2id), command registry, and first-run bootstrap. Also accessible via the WebSocket CLI bridge in the web dashboard.
- **Health monitoring** — periodic database connectivity and disk space checks.
- **Persistence** — SQLite with SQLCipher encryption, JDBI SQL Object interfaces, HikariCP connection pool.

Dependency wiring is split across six `@Configuration` classes: `JdbiConfiguration` (datasource, JDBI, repositories), `ServiceConfiguration` (services, serialization, bridge beans), `HealthConfiguration` (health checks, scheduler), `IngressConfiguration` (MQTT listener, telemetry handler), `CliConfiguration` (conditional CLI startup), and `WebSocketConfiguration` (STOMP broker, SockJS endpoint).

---

## Runtime Flows

### 1. Node Startup and Heartbeat

```
NodeApplication.main()
  │
  ├─ AppLifeCycle.init()
  │    ├─ Creates AppContainer (HashMap-based bean registry)
  │    ├─ AspectProcessor scans for annotations (scaffold)
  │    └─ AppContainer.init()
  │         ├─ ConfigLoader loads application.yml + env var overrides
  │         ├─ Wires: BatteryProvider, PahoMqttProvider, ObjectMapper,
  │         │         HeartbeatService, NodeRuntime
  │         └─ All dependencies registered via constructor injection
  │
  ├─ Registers JVM shutdown hook → lifecycle.close()
  │
  └─ NodeRuntime.start()
       ├─ State: SHUTDOWN → STARTING
       ├─ Spawns Connection Supervisor (virtual thread)
       │    └─ Connects to MQTT broker with exponential backoff
       │         (1s → 2s → 4s → ... → 60s max)
       ├─ State: STARTING → RUNNING
       ├─ Spawns Heartbeat Loop (virtual thread)
       │    └─ Every 30s:
       │         HeartbeatService.sendHeartbeat()
       │           ├─ BatteryProvider.getBatteryLevel()
       │           ├─ Build TelemetryPacketDto (JSON)
       │           └─ MqttProvider.publish(topic, payload)
       └─ Spawns Watchdog (virtual thread)
            └─ Every 60s:
                 ├─ Check battery level (warn if < 15%)
                 └─ Log connection state
```

### 2. Telemetry Ingress and Threat Detection

This is the core data flow — what happens when telemetry arrives at the controller:

```
MQTT Message arrives on avisos/telemetry
  │
  ├─ MqttIngressListener (Paho callback, dispatches to virtual thread)
  │
  ├─ MqttIngressAdapter
  │    └─ Converts Paho MqttMessage → IngressMessage (topic, payload, timestamp)
  │
  └─ TelemetryIngressHandler.handle(IngressMessage)
       │
       ├─ Deserialize JSON → TelemetryPacketDto
       │    (malformed packets are logged and discarded)
       │
       ├─ NodeService.updateNodeHeartbeat(dto)
       │    ├─ Flood check: reject if < minimum interval since last heartbeat
       │    ├─ Upsert node in SQLite (INSERT OR REPLACE)
       │    └─ Update in-memory cache
       │
       ├─ If packet type == HEARTBEAT → return (no further processing)
       │
       ├─ VisionService.analyze(image data)
       │    └─ CodeProjectVisionClient → HTTP POST multipart/form-data
       │         → CodeProject.AI /v1/vision/detection
       │
       ├─ Extract detected labels from VisionResponse
       │
       ├─ ThreatDetector.evaluate(labels)
       │    └─ KeywordThreatDetector:
       │         ├─ Case-insensitive substring match against critical keywords
       │         ├─ Then against warning keywords
       │         └─ Returns AlarmSeverity (CRITICAL > WARNING > NONE)
       │
       └─ If severity > NONE:
            └─ AlarmService.saveAlarm(severity, labels, nodeId)
                 └─ Persisted to SQLite alarms table
```

### 3. Controller Startup and CLI

```
AvisosControllerServiceApplication.main()
  │
  └─ SpringApplication.run()
       │
       ├─ Spring Boot auto-configuration
       │    ├─ Embedded Tomcat starts on port 8080
       │    └─ Actuator endpoints: /actuator/health, /actuator/info, /actuator/metrics
       │
       ├─ @Configuration classes initialize beans:
       │    ├─ JdbiConfiguration
       │    │    ├─ DataSource (HikariCP + EncryptingDataSource + SQLCipher)
       │    │    ├─ Jdbi instance with SqlObjectPlugin
       │    │    └─ Repositories via jdbi.onDemand() + table init
       │    ├─ ServiceConfiguration
       │    │    ├─ Bridge beans (properties → domain config records)
       │    │    ├─ ObjectMapper (JavaTimeModule + ParameterNamesModule)
       │    │    └─ Services (alarm, node, vision, auth, threat, notification)
       │    ├─ IngressConfiguration
       │    │    └─ MqttIngressListener (@PostConstruct subscribes to topic)
       │    ├─ HealthConfiguration
       │    │    └─ Health scheduler (every 10s) + stale node cleanup
       │    └─ CliConfiguration (conditional: avisos.cli.enabled=true)
       │         ├─ CliClient, CommandRegistry (9 commands), CliService
       │         └─ CommandLineRunner spawns CLI on non-daemon thread
       │
       ├─ @PreDestroy hooks handle graceful shutdown
       │    ├─ ShutdownManager.initiate()
       │    └─ MqttIngressListener.shutdown()
       │
       └─ If CLI enabled:
            JLineCliService.start()
              │
              ├─ Phase 1: Authentication
              │    ├─ If no users exist → bootstrap: create first admin account
              │    └─ Prompt username/password → AuthService.authenticate()
              │         └─ Argon2id verify → set SecurityContext
              │
              └─ Phase 2: Command Loop
                   └─ Read input → CommandRegistry.find() → execute on virtual thread
                        Available commands: about, alarms, exit, health,
                                            help, inspect, nodes, purge, stats
```

### 4. Web Dashboard and Real-Time Updates

```
Browser loads http://localhost:8080
  │
  ├─ SpaForwardingController → forward:/index.html
  │    └─ React SPA loads (Vite-built bundle from /static/)
  │
  ├─ REST API (initial data load)
  │    ├─ GET /api/nodes    → NodeController → NodeService
  │    ├─ GET /api/alarms   → AlarmController → AlarmService
  │    ├─ GET /api/health   → HealthController → SystemHealthMonitor
  │    └─ GET /api/system/* → SystemController → JVM runtime stats
  │
  └─ WebSocket (live updates)
       │
       ├─ STOMP connect to /ws (SockJS)
       │
       ├─ Subscribe /topic/nodes
       │    └─ Receives NodeRecord on every heartbeat
       │         ← DashboardEventBroadcaster ← NodeHeartbeatEvent
       │              ← TelemetryIngressHandler (ApplicationEventPublisher)
       │
       ├─ Subscribe /topic/alarms
       │    └─ Receives AlarmRecord on new alarm creation
       │         ← DashboardEventBroadcaster ← AlarmCreatedEvent
       │
       ├─ Subscribe /topic/vision
       │    └─ Receives {nodeId, VisionResponse} on analysis
       │         ← DashboardEventBroadcaster ← VisionAnalysisEvent
       │
       └─ CLI Terminal
            ├─ User types command in xterm.js widget
            ├─ STOMP publish to /app/cli (CliCommandMessage)
            ├─ WebSocketCliController.handleCommand()
            │    ├─ Creates BufferingCliClient (per-request)
            │    ├─ Creates InMemoryCommandRegistry (per-request)
            │    ├─ Registers all commands (except ExitCommand)
            │    ├─ Finds and executes command
            │    └─ Returns CliResponseMessage (output + executionMs)
            └─ Response delivered to /topic/cli → rendered in terminal
```

### 5. Health Monitoring

```
Scheduled every 10 seconds:
  │
  SystemHealthMonitor.checkSystemHealth()
    │
    ├─ DatabaseHealthCheck
    │    └─ Execute "SELECT 1" with 500ms timeout via Future
    │         → HEALTHY (with latency) or UNHEALTHY (timeout/error)
    │
    ├─ Disk Space Check
    │    └─ File("/").getUsableSpace()
    │         → HEALTHY (>= 100 MB) or DEGRADED (< 100 MB)
    │
    └─ Compute overall status:
         Any UNHEALTHY → system UNHEALTHY
         Else any DEGRADED → system DEGRADED
         Else → HEALTHY
```

---

## Data Model

### SQLite Tables

| Table | Primary Key | Purpose |
|---|---|---|
| `nodes` | `uuid` (TEXT) | Registered edge nodes with status, battery level, last_seen |
| `alarms` | `id` (TEXT, UUID) | Triggered alarms with severity, reason, status (ACTIVE/RESOLVED) |
| `telemetry_audit` | auto | Audit log of received telemetry packets |
| `users` | `username` (TEXT) | Operator accounts with Argon2id password hashes |

All timestamps are stored as TEXT in ISO 8601 format. The database is encrypted with SQLCipher — the encryption key is loaded from the `.env` file (`DATABASE_ENCRYPTION_KEY`).

### Domain Models

Domain objects are Java records (immutable). The codebase maintains a clear separation between:

- **Entities** (`entity/`) — database row representations, used by JDBI mappers.
- **Models** (`model/`) — domain objects used by services and the CLI.
- **DTOs** (`common/`) — serialization types for MQTT telemetry.
- **Mappers** (`mapper/`) — convert between entities and domain models.

---

## Concurrency Model

Both services use Java 21+ **virtual threads** (`Executors.newVirtualThreadPerTaskExecutor()`):

- **Node service** — three virtual-thread loops (connection, heartbeat, watchdog) managed by `NodeRuntime`.
- **Controller service** — MQTT message handling, CLI command execution, and health checks all run on virtual threads.

Thread safety considerations:

- `SimpleNodeService` uses `ConcurrentHashMap` for the in-memory node cache and `AtomicLong` for metrics counters.
- `SecurityContext` uses `InheritableThreadLocal` so child virtual threads inherit the authenticated user from the parent thread.
- `DeduplicatingLogger` uses `ConcurrentHashMap` with `AtomicInteger` counters.
- `SystemHealthMonitor` stores the latest health report in a `volatile` field.

---

## Security Model

- **Password hashing** — Argon2id (3 iterations, 64 MB memory, 1 thread).
- **Session context** — `InheritableThreadLocal<UserRecord>` cleared after use.
- **Database encryption** — SQLCipher with PRAGMA key applied per connection.
- **First-run bootstrap** — when no users exist, the CLI prompts to create the initial operator account.
- **Role field** — stored in the database but role-based access control is not yet enforced.

---

## Configuration

### Controller Service

Configuration is managed via Spring Boot `@ConfigurationProperties` records bound to the `avisos.*` prefix in `application.yml`. Spring Boot's standard environment variable binding applies (e.g., `AVISOS_MQTT_BROKER` overrides `avisos.mqtt.broker`).

| Properties class | Prefix | Key fields |
|---|---|---|
| `AvisosMqttProperties` | `avisos.mqtt` | controllerClientId, broker, topic, connectionTimeout, cleanSession, automaticReconnect |
| `AvisosVisionProperties` | `avisos.vision` | apiUrl, minConfidence, timeoutSeconds |
| `AvisosDatabaseProperties` | `avisos.database` | url |
| `AvisosNodeServiceProperties` | `avisos.node-service` | staleThreshold, minHeartbeatIntervalMs |
| `AvisosCliProperties` | `avisos.cli` | enabled |

Legacy environment variables still work for Docker Compose compatibility:

| Variable | Purpose |
|---|---|
| `MQTT_BROKER_URL` | MQTT broker connection string |
| `MQTT_TOPIC` | Telemetry subscription topic |
| `VISION_API_URL` | CodeProject.AI endpoint |
| `DATABASE_URL` | SQLite JDBC connection string |
| `DATABASE_ENCRYPTION_KEY` | SQLCipher encryption key (from `.env`) |
| `AVISOS_CLI_ENABLED` | Enable JLine CLI (`true`/`false`, default `false`) |

### Node Service

Configuration is loaded from `application.yml` by the custom `ConfigLoader` (in `framework/` package) with environment variable overrides taking precedence.

| Variable | Default | Purpose |
|---|---|---|
| `MQTT_BROKER_URL` | from YAML | MQTT broker connection string |
| `MQTT_TOPIC` | from YAML | Telemetry publish topic |
| `NODE_NAME` | from YAML | Human-readable node identifier |
| `NODE_TYPE` | from YAML | Device classification |

---

## Infrastructure Services (Docker Compose)

| Service | Image | Port | Purpose |
|---|---|---|---|
| `controller` | Custom (controller.Dockerfile) | 8080 | Central orchestration service (Spring Boot, CLI disabled) |
| `node-01` | Custom (node.Dockerfile) | — | Sample edge node |
| `mosquitto` | eclipse-mosquitto:latest | 1883 | MQTT message broker |
| `localstack` | localstack/localstack:3 | 4567→4566 | AWS SNS emulation |
| `vision-api` | codeproject/ai-server | 32168 | CodeProject.AI object detection |

All services run on the `avisos-net` bridge network. The controller depends on mosquitto and vision-api. Nodes depend on mosquitto only.

---

## Key Design Decisions

### Spring Boot for Controller, Custom IoC for Node

The controller service uses Spring Boot 3.4.1 for auto-configuration, component scanning, embedded web server, and actuator endpoints. The Spring Boot BOM is imported (not used as parent POM) to preserve the `avisos-parent` Maven hierarchy. `DataSourceAutoConfiguration` is excluded since the datasource is manually managed (encrypted SQLite via `EncryptingDataSource`). JDBI repositories are created as `@Bean` methods via `jdbi.onDemand()` since they cannot be annotated with Spring stereotypes.

The node service uses a hand-rolled IoC container (`framework/` package) ported from the controller's original custom framework. This keeps the edge deployment footprint minimal — no Spring Boot overhead, fast startup, transparent wiring. All dependencies are explicitly visible in `AppContainer.init()`.

### JDBI over JPA/Hibernate

JDBI SQL Object interfaces provide lightweight, SQL-first data access without the complexity of an ORM. Repositories are created via `jdbi.onDemand()` which handles connection acquisition and release automatically. This pairs well with SQLite, which has limited ORM support.

### MQTT over HTTP/gRPC for telemetry

Nodes publish telemetry over MQTT (pub/sub) rather than making direct HTTP calls to the controller. This decouples node availability from controller availability — nodes can publish even when the controller is temporarily down, and the broker handles message buffering. A gRPC approach was attempted and rolled back (see git history).

### SQLite with SQLCipher

An embedded database avoids the operational overhead of a separate database server. SQLCipher adds encryption at rest. WAL mode is enabled for concurrent read performance. The trade-off is single-writer throughput, which is acceptable for the expected node count.

### Virtual threads everywhere

Both services use Java 21+ virtual threads for all concurrent work. This eliminates the need for reactive frameworks or manual thread pool sizing — virtual threads are cheap enough to create per-task without pooling concerns.
