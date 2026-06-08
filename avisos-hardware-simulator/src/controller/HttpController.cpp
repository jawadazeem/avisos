/**
 * @file HttpController.cpp
 * @brief Implementation of the REST endpoint serving simulator telemetry.
 *
 * Uses cpp-httplib (single-header HTTP server). The server binds to
 * 0.0.0.0 so it is reachable from other containers on the Docker network.
 */

#include "HttpController.h"

#include <httplib.h>
#include <nlohmann/json.hpp>
#include <spdlog/spdlog.h>

namespace avisos::controller {

HttpController::HttpController(avisos::simulator::Simulator& simulator)
    : simulator_(simulator) {}

void HttpController::start(int port) {
    server_thread_ = std::thread([this, port]() {
        httplib::Server server;

        server.Get("/readings", [this](const httplib::Request&, httplib::Response& res) {
            auto snapshot = simulator_.latest_snapshot();
            nlohmann::json j = snapshot;
            res.set_content(j.dump(), "application/json");
        });

        server.Get("/health", [](const httplib::Request&, httplib::Response& res) {
            res.set_content(R"({"status":"UP"})", "application/json");
        });

        spdlog::info("HTTP server listening on port {}", port);
        server.listen("0.0.0.0", port);
    });
}

void HttpController::stop() {
    spdlog::info("HTTP server stopping...");
    if (server_thread_.joinable()) {
        server_thread_.join();
    }
}

} // namespace avisos::controller
