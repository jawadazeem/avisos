/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * @file FrameProvider.cpp
 * @brief Loads PNG/JPG frames from alarm/ and normal/ subdirectories.
 */

#include "FrameProvider.h"

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

    for (const auto& entry : fs::directory_iterator(path)) {
        if (!entry.is_regular_file()) continue;

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
    bool use_alarm = false;

    if (!alarm_frames_.empty() && !normal_frames_.empty()) {
        int roll = weight_dist_(gen_);
        use_alarm = (roll <= 10); // 10% alarm, 90% normal
    } else if (!alarm_frames_.empty()) {
        use_alarm = true;
    }
    // else: normal only (or empty, but caller checks availability)

    if (use_alarm) {
        std::uniform_int_distribution<size_t> idx(0, alarm_frames_.size() - 1);
        return alarm_frames_[idx(gen_)];
    } else {
        std::uniform_int_distribution<size_t> idx(0, normal_frames_.size() - 1);
        return normal_frames_[idx(gen_)];
    }
}

} // namespace avisos::service
