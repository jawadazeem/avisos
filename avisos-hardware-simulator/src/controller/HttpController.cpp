/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

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

HttpController::HttpController(avisos::simulator::Simulator& simulator,
                               avisos::service::FrameProvider& frame_provider)
    : simulator_(simulator), frame_provider_(frame_provider) {}

void HttpController::start(int port) {
    server_thread_ = std::thread([this, port]() {
        httplib::Server server;

        server.Get("/readings", [this](const httplib::Request&, httplib::Response& res) {
            auto snapshot = simulator_.latest_snapshot();
            nlohmann::json j = snapshot;
            res.set_content(j.dump(), "application/json");
        });

        server.Get("/frame", [this](const httplib::Request&, httplib::Response& res) {
            if (frame_provider_.alarm_count() == 0 && frame_provider_.normal_count() == 0) {
                res.status = 404;
                res.set_content(R"({"error":"no frames loaded"})", "application/json");
                return;
            }
            const auto& frame = frame_provider_.pick_frame();
            res.set_content(
                std::string(reinterpret_cast<const char*>(frame.data()), frame.size()),
                "image/png");
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
