#include <csignal>
#include <spdlog/spdlog.h>

#include "service/Simulator.h"
#include "controller/HttpController.h"

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
