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
    void start();
    void stop();

    /** Returns a thread-safe copy of the latest snapshot (called by HttpController). */
    HardwareSnapshot latest_snapshot() const;

    /** Returns node metadata. */
    const NodeMetaData& node() const;

private:
    HardwareSnapshot random_snapshot();

    NodeMetaData node_;
    HardwareSnapshot snapshot_;

    std::atomic<bool> running_{false};
    std::thread loop_thread_;
    mutable std::mutex snapshot_mutex_;
};

} // namespace avisos::simulator
