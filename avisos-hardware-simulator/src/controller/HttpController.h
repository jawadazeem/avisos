#pragma once

#include "../service/Simulator.h"

#include <thread>

namespace avisos::controller {

/**
 * HTTP server that exposes the simulator's latest snapshot.
 *
 * The node service polls GET /readings on port 5000 to retrieve
 * hardware telemetry as JSON.
 */
class HttpController {
public:
    static constexpr int DEFAULT_PORT = 5000;

    explicit HttpController(avisos::simulator::Simulator& simulator);

    void start(int port = DEFAULT_PORT);
    void stop();

private:
    avisos::simulator::Simulator& simulator_;
    std::thread server_thread_;
};

} // namespace avisos::controller
