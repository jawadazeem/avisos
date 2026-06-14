/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * @file FrameProvider.cpp
 * @brief Loads PNG/JPG frames from alarm/ and normal/ subdirectories.
 */

#include "FrameProvider.h"

#include <algorithm>
#include <filesystem>
#include <fstream>
#include <spdlog/spdlog.h>

namespace fs = std::filesystem;

namespace avisos::service {

FrameProvider::FrameProvider(const std::string& frames_dir) {
    std::string alarm_path = frames_dir + "/alarm";
    std::string normal_path = frames_dir + "/normal";

    load_directory(alarm_path, alarm_frames_);
    load_directory(normal_path, normal_frames_);

    spdlog::info("FrameProvider loaded {} alarm frames, {} normal frames",
                 alarm_frames_.size(), normal_frames_.size());

    if (alarm_frames_.empty() && normal_frames_.empty()) {
        spdlog::warn("No frames found in {} — /frame endpoint will return 404", frames_dir);
    }
}

void FrameProvider::load_directory(const std::string& path,
                                   std::vector<std::vector<uint8_t>>& target) {
    if (!fs::exists(path) || !fs::is_directory(path)) {
        spdlog::warn("Frame directory not found: {}", path);
        return;
    }

    std::vector<fs::directory_entry> entries;
    for (const auto& entry : fs::directory_iterator(path)) {
        if (entry.is_regular_file()) entries.push_back(entry);
    }

    std::sort(entries.begin(), entries.end(), [](const auto& left, const auto& right) {
        return left.path().filename().string() < right.path().filename().string();
    });

    for (const auto& entry : entries) {
        auto ext = entry.path().extension().string();
        if (ext != ".png" && ext != ".jpg" && ext != ".jpeg") continue;

        std::ifstream file(entry.path(), std::ios::binary);
        if (!file) {
            spdlog::warn("Could not open frame: {}", entry.path().string());
            continue;
        }

        auto size = fs::file_size(entry.path());
        std::vector<uint8_t> buffer(size);
        file.read(reinterpret_cast<char*>(buffer.data()), static_cast<std::streamsize>(size));

        spdlog::debug("Loaded frame: {} ({} bytes)", entry.path().filename().string(), size);
        target.push_back(std::move(buffer));
    }
}

const std::vector<uint8_t>& FrameProvider::pick_frame() {
    std::lock_guard<std::mutex> lock(frame_mutex_);

    if (!alarm_frames_.empty()) {
        const auto& frame = alarm_frames_[next_alarm_index_];
        next_alarm_index_ = (next_alarm_index_ + 1) % alarm_frames_.size();
        return frame;
    }

    const auto& frame = normal_frames_[next_normal_index_];
    next_normal_index_ = (next_normal_index_ + 1) % normal_frames_.size();
    return frame;
}

} // namespace avisos::service
