# Future Directions

Numbered roadmap items for AVISOS. When an agent is tasked with work, it will reference one of these items by number.

---

## 1. Spring Boot Migration (Controller) + Custom Framework Reuse (Node) -- COMPLETED

**Status:** Implemented. See commit history for full changeset.

**What was done:**

*Controller Service -- migrated to Spring Boot 3.4.1:*
- Added Spring Boot BOM (imported, not as parent POM) with starters: web, actuator, validation, test.
- Replaced `maven-shade-plugin` with `spring-boot-maven-plugin` for executable jar packaging.
- Entry point is now `@SpringBootApplication` with `SpringApplication.run()`.
- Created 5 `@ConfigurationProperties` record classes under `avisos.*` prefix for type-safe config binding.
- Created 5 `@Configuration` classes (`JdbiConfiguration`, `ServiceConfiguration`, `HealthConfiguration`, `IngressConfiguration`, `CliConfiguration`) replacing the manual `AppContainer.init()` wiring.
- Annotated service/infrastructure classes with `@Service`, `@Component` for component scanning.
- JLine CLI gated by `@ConditionalOnProperty("avisos.cli.enabled")`, default `false` in web/Docker mode.
- Actuator endpoints exposed at `/actuator/health`, `/actuator/info`, `/actuator/metrics`.
- Deleted `framework/` package (5 files), `JdbiProvider`, and mutable `AppConfig` from controller.

*Node Service -- adopted the custom DIY IoC framework:*
- Ported `AppContainer`, `AppLifeCycle`, `ConfigLoader`, `AspectProcessor` into `node/framework/`.
- Simplified `NodeApplication.main()` to use `AppLifeCycle` lifecycle coordination.
- Updated `ConfigLoaderTest` for instance-based API.
- Deleted redundant `config/ConfigLoader` and `config/ConfigResolver`.

*Infrastructure:*
- `docker-compose.yml`: exposed port 8080, added `AVISOS_CLI_ENABLED=false`, removed `stdin_open`/`tty`.
- `controller.Dockerfile`: no changes needed (same jar name and entrypoint).
- `CLAUDE.md`: updated to reflect new architecture.

---

## 2. Web Dashboard with Embedded CLI -- COMPLETED

**Status:** Implemented. See commit history for full changeset.

**What was done:**

*Backend -- REST API layer:*
- Created 4 `@RestController` classes (`NodeController`, `AlarmController`, `HealthController`, `SystemController`) exposing existing services as JSON endpoints under `/api/`.
- Added `SpaForwardingController` to forward non-API paths to `index.html` for React Router client-side routing.
- Added `GlobalExceptionHandler` (`@RestControllerAdvice`) for consistent JSON error responses.

*Backend -- WebSocket infrastructure:*
- Added `spring-boot-starter-websocket` dependency.
- Created `WebSocketConfiguration` (`@EnableWebSocketMessageBroker`) with STOMP protocol, simple broker on `/topic`, app prefix `/app`, SockJS fallback at `/ws`.
- Created 3 domain event classes (`AlarmCreatedEvent`, `NodeHeartbeatEvent`, `VisionAnalysisEvent`) using Spring's `ApplicationEvent`.
- Modified `TelemetryIngressHandler` to publish domain events via `ApplicationEventPublisher` after processing heartbeats, alarms, and vision analysis.
- Created `DashboardEventBroadcaster` (`@EventListener`) that forwards domain events to STOMP topics (`/topic/alarms`, `/topic/nodes`, `/topic/vision`).

*Backend -- WebSocket CLI bridge:*
- Created `BufferingCliClient` (implements `CliClient`) that captures `println()` output to a `StringBuilder` instead of a terminal.
- Created `WebSocketCliController` (`@MessageMapping("/cli")`, `@SendTo("/topic/cli")`) that creates a fresh `BufferingCliClient` and `InMemoryCommandRegistry` per request, registers all commands except `ExitCommand`, executes the command, and returns the buffered output with execution time.
- Created `CliCommandMessage` and `CliResponseMessage` records for WebSocket message serialization.

*Frontend -- React + TypeScript + Vite:*
- Scaffolded React 19 project with Vite, TypeScript, and SCADA dark-mode theme in `src/main/frontend/`.
- Integrated `frontend-maven-plugin` (eirslett) in the Maven build to install Node v22, run `npm install`, and build the frontend to `src/main/resources/static/` during `generate-resources` phase.
- Created SCADA-themed CSS with dark backgrounds (`#0a0e14`), green accent (`#00ff88`), amber warnings, red alerts, monospace JetBrains Mono font.
- Created TypeScript models mirroring all Java domain records and enums.
- Created typed REST API client and WebSocket context/hooks (`@stomp/stompjs` + `sockjs-client`).
- Built 4 pages: Dashboard (health + node + alarm + stats panels), Nodes (table with battery bars and live heartbeat updates), Alarms (sortable table with resolve buttons and critical row pulse animation), CLI (xterm.js terminal widget with quick-command toolbar).
- Built reusable UI components: `StatusBadge` (auto-colored by status), `DataCard` (SCADA-styled panel), `Sidebar` (nav with active indicators), `Header` (connection status dot + 24h clock).
- xterm.js terminal widget with STOMP integration: sends commands to `/app/cli`, receives responses from `/topic/cli`, displays ASCII banner on startup.

*Infrastructure:*
- Updated `.gitignore` with `node_modules/`, `node/`, and `static/` exclusions for the frontend build.

---

## 3. AI SOC Analyst Agent

Integrate an AI agent (Claude or similar LLM) that acts as an automated SOC (Security Operations Center) analyst within the vision-detection pipeline.

**Workflow:**
1. A camera frame is submitted to CodeProject.AI for object detection.
2. The detection results (matched labels, confidence scores) and the source image are forwarded to the AI agent.
3. The agent analyzes the image alongside the matched keywords and contextual telemetry.
4. The agent produces structured outputs:
   - **Alarm record** -- severity, classification, recommended response, formatted for the alarm subsystem.
   - **Email notification** -- human-readable summary with the image attached, sent to the configured distribution list.
   - **SMS/text alert** -- concise message for on-call personnel.
5. These artifacts are pushed through the existing notification service to the appropriate recipients.

The agent augments -- not replaces -- the rule-based alarm logic already in place, adding natural-language reasoning and contextual triage that static keyword matching cannot provide.
