/**
 * @file Simulator.h
 * @brief Core simulation engine that periodically generates hardware telemetry.
 *
 * Runs a dedicated background thread that produces a new HardwareSnapshot
 * every 30 seconds. Snapshot quality is randomised across three tiers
 * (shambles / average / strong) to simulate a realistic fleet of sensors.
 *
 * Thread safety: the latest snapshot is guarded by a mutex so the
 * HttpController can read it concurrently via latest_snapshot().
 */

#pragma once

#include "../model/HardwareSnapshot.h"
#include "../model/NodeMetaData.h"

#include <atomic>
#include <mutex>
#include <thread>

namespace avisos::simulator {

using avisos::model::NodeMetaData;
using avisos::model::HardwareSnapshot;

class Simulator {
public:
    /** Spawns the simulation loop on a background thread. */
    void start();

    /** Signals the loop to stop and blocks until the thread exits. */
    void stop();

    /** Returns a thread-safe copy of the latest snapshot (called by HttpController). */
    [[nodiscard]] HardwareSnapshot latest_snapshot() const;

    /** Returns node identity metadata (UUID, name, camera type). */
    [[nodiscard]] const NodeMetaData& node() const;

private:
    /** Rolls a weighted random to pick shambles (10%), average (80%), or strong (10%). */
    HardwareSnapshot random_snapshot();

    NodeMetaData node_;
    HardwareSnapshot snapshot_;

    std::atomic<bool> running_{false};
    std::thread loop_thread_;
    mutable std::mutex snapshot_mutex_;  // guards snapshot_ for concurrent reads
};

} // namespace avisos::simulator
