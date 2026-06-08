/**
 * @file Simulator.cpp
 * @brief Implementation of the simulation loop and snapshot generation.
 */

#include "Simulator.h"

#include <chrono>
#include <random>
#include <spdlog/spdlog.h>

namespace avisos::simulator {

/// How often the simulation loop generates a fresh snapshot.
static constexpr auto SIMULATION_INTERVAL = std::chrono::seconds(30);

HardwareSnapshot Simulator::random_snapshot() {
    std::mt19937 gen(std::random_device{}());
    std::uniform_int_distribution<int> dist(1, 100);
    int roll = dist(gen);

    if (roll <= 10) {
        return HardwareSnapshot::shambles();
    } else if (roll <= 90) {
        return HardwareSnapshot::average();
    } else {
        return HardwareSnapshot::strong();
    }
}

void Simulator::start() {
    node_ = NodeMetaData();
    spdlog::info("Node: {} ({})", node_.name(), node_.get_type_as_string());

    running_ = true;
    loop_thread_ = std::thread([this]() {
        while (running_) {
            {
                std::lock_guard<std::mutex> lock(snapshot_mutex_);
                snapshot_ = random_snapshot();
            }

            spdlog::info("Snapshot — battery: {}%, temp: {:.1f}°C, signal: {}%, leak: {}",
                         snapshot_.battery_percent(),
                         snapshot_.temperature_celsius(),
                         snapshot_.signal_quality_percent(),
                         snapshot_.leak_detected());

            std::this_thread::sleep_for(SIMULATION_INTERVAL);
        }
    });

    spdlog::info("Simulation loop started (interval: {}s)",
                 SIMULATION_INTERVAL.count());
}

void Simulator::stop() {
    spdlog::info("Shutting down simulator...");
    running_ = false;
    if (loop_thread_.joinable()) {
        loop_thread_.join();
    }
    spdlog::info("Simulator stopped.");
}

HardwareSnapshot Simulator::latest_snapshot() const {
    std::lock_guard<std::mutex> lock(snapshot_mutex_);
    return snapshot_;
}

const NodeMetaData& Simulator::node() const {
    return node_;
}

} // namespace avisos::simulator
