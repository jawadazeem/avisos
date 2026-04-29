# AVISOS
***A***dvanced ***V***isual ***I***nfrastructure ***S***ecure ***O***perational ***S***ystems

#### Author: Jawad Azeem
#### Project Documentation: www.jawadazeem.com/avisos
AVISOS is an industrial-grade orchestration platform designed to secure and manage high-reliability SCADA environments. Built on a custom-engineered backbone, it integrates Generative AI for predictive threat modeling with a hardened command-bus to ensure the integrity of critical infrastructure operations.

---
## Architectural Infrastructure
The system focuses on "Infrastructure as Code" by implementing a micro-framework to handle service lifecycles and cross-cutting concerns.

### 1. Custom IoC & Dependency Management
The system utilizes a custom Inversion of Control (IoC) container to decouple service instantiation from business logic.
- Reflection-Based Scanning: The AppContainer scans the classpath at startup to instantiate services and wire dependencies.
- Metadata-Driven Execution: Custom annotations (e.g., @Timed, @DataSensitive) allow the system to inject behavior at runtime based on method-level metadata.

### 2. Dynamic Proxy Interception (AOP)
To maintain the Single Responsibility Principle, Sentinel utilizes JDK Dynamic Proxies to intercept method calls without modifying core source code.
- Latency Monitoring: Methods marked with @Timed are automatically wrapped in a PerformanceHandler via InvocationHandler.
- Zero-Touch Instrumentation: Performance metrics and threshold violations are captured at the proxy layer, ensuring the core SecurityHub logic remains focused on security orchestration.

### 3. Command-Pattern Orchestration
- Encapsulated Actions: Every hardware interaction is a discrete Command object, allowing for audit logging, re-tries, and prioritization.
- Priority-Queue Dispatching: High-priority events (such as PanicCommand) utilize a Deque to bypass the standard processing pipeline, ensuring sub-millisecond response for life-safety events.

---

## Key Components

### Core Infrastructure

* **AppContainer**: The system backbone. It manages object lifecycles and performs the "Proxy Swap" for annotated services.
* **SecurityHub**: The central orchestrator. It manages the task queue, device registry, and global arming status.
* **PerformanceHandler**: A dynamic interceptor that enforces performance thresholds defined in @Timed annotations. 

### Devices

* **Device Interface**: Defines the contract for all hardware sensors, including health checks and subscriber management.
* **BaseDevice**: An abstract layer providing shared state logic (failure counts, status tracking) to minimize code duplication across sensor types.
* **Sensor Implementations:**: Concrete classes for Motion, Smoke, and Thermal sensors containing hardware-specific logic.

---

## Technical Features

* **Priority Processing**: `PanicCommand` utilizes a Double-Ended Queue (Deque) to bypass the standard processing order, ensuring life-safety events are prioritized.
* **Defensive Programming**: The Hub implements null-safety checks and status validation to prevent runtime exceptions during command dispatching.
* **Subscriber Model**: Implements the Observer pattern to decouple sensors from notification services like the Police Station or Mobile Applications.
* **Persistence Layer**: Utilizes a hand-written JDBC implementation to efficiently manage hardware state and audit logs, avoiding the overhead of heavy ORM frameworks.
