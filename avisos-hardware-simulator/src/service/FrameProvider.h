/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * @file FrameProvider.h
 * @brief Loads camera frame images from disk and serves alarm frames in a
 *        deterministic rotation for repeatable demos.
 *
 * On construction, scans the alarm/ and normal/ subdirectories under the
 * given frames root, reads every file into memory, and stores the raw bytes.
 * pick_frame() returns a const reference to one of those buffers.
 */

#pragma once

#include <mutex>
#include <random>
#include <string>
#include <vector>

namespace avisos::service {

class FrameProvider {
public:
    explicit FrameProvider(const std::string& frames_dir);

    /** Returns the next alarm frame, falling back to normal frames if none exist. */
    [[nodiscard]] const std::vector<uint8_t>& pick_frame();

    [[nodiscard]] size_t alarm_count() const { return alarm_frames_.size(); }
    [[nodiscard]] size_t normal_count() const { return normal_frames_.size(); }

private:
    void load_directory(const std::string& path, std::vector<std::vector<uint8_t>>& target);

    std::vector<std::vector<uint8_t>> alarm_frames_;
    std::vector<std::vector<uint8_t>> normal_frames_;
    std::mutex frame_mutex_;
    size_t next_alarm_index_{0};
    size_t next_normal_index_{0};
};

} // namespace avisos::service
