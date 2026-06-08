/**
 * @file HttpController.h
 * @brief HTTP layer that exposes simulator telemetry to the Java node service.
 *
 * The node service's HttpHardwareTelemetryProvider polls GET /readings
 * every heartbeat cycle. This controller serves the latest HardwareSnapshot
 * as JSON, with field names matching the Java HardwareSnapshot record
 * (camelCase) for zero-config deserialization.
 *
 * Endpoints:
 *   GET /readings — latest HardwareSnapshot as JSON
 *   GET /health   — simple {"status":"UP"} liveness check
 */

#pragma once

#include "../service/Simulator.h"

#include <thread>

namespace avisos::controller {

class HttpController {
public:
    static constexpr int DEFAULT_PORT = 5000;

    /** @param simulator Reference to the simulator whose snapshots are served. */
    explicit HttpController(avisos::simulator::Simulator& simulator);

    /** Spawns the HTTP server on a background thread. */
    void start(int port = DEFAULT_PORT);

    /** Blocks until the server thread exits. */
    void stop();

private:
    avisos::simulator::Simulator& simulator_;
    std::thread server_thread_;
};

} // namespace avisos::controller
