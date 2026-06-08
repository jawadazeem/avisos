/**
 * @file main.cpp
 * @brief Entry point for the Avisos Hardware Simulator.
 *
 * Wires together the simulation loop (Simulator) and the HTTP server
 * (HttpController), then blocks until a SIGINT or SIGTERM triggers
 * graceful shutdown of both components.
 *
 * Lifecycle:
 *   1. Simulator::start()      — spawns background thread generating snapshots
 *   2. HttpController::start() — spawns thread serving GET /readings on :5000
 *   3. Main thread blocks on controller.stop() (join)
 *   4. Signal → handler calls stop() on both → threads exit → main returns
 */

#include <csignal>
#include <spdlog/spdlog.h>

#include "service/Simulator.h"
#include "controller/HttpController.h"

// Global pointers for signal handler access. Signal handlers are plain C
// functions and cannot capture state, so we store raw (non-owning) pointers
// at file scope. The objects themselves live on main()'s stack.
static avisos::simulator::Simulator* g_simulator = nullptr;
static avisos::controller::HttpController* g_controller = nullptr;

void signal_handler(int signal) {
    spdlog::info("Received signal {}, shutting down...", signal);
    if (g_controller) g_controller->stop();
    if (g_simulator) g_simulator->stop();
}

int main() {
    spdlog::set_pattern("[%Y-%m-%d %H:%M:%S] [%l] %v");
    spdlog::info("Avisos Hardware Simulator v0.1.0");

    avisos::simulator::Simulator simulator;
    avisos::controller::HttpController controller(simulator);

    g_simulator = &simulator;
    g_controller = &controller;

    std::signal(SIGINT, signal_handler);
    std::signal(SIGTERM, signal_handler);

    simulator.start();
    controller.start();

    // Block main thread until the HTTP server thread exits (on stop/signal)
    controller.stop();
    simulator.stop();

    spdlog::info("Goodbye.");
    return 0;
}
