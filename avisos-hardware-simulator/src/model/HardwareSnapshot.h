#pragma once

#include <chrono>
#include <string>

#include <nlohmann/json.hpp>

namespace avisos::model {

/**
 * Point-in-time hardware vitals produced by the simulation loop.
 *
 * Field names match the Java HardwareSnapshot record exactly so the
 * node service can deserialize the JSON without any mapping layer.
 */
class HardwareSnapshot {
public:
    using Clock = std::chrono::system_clock;
    using Instant = std::chrono::time_point<Clock>;

    HardwareSnapshot();

    HardwareSnapshot(int battery_percent,
                     double temperature_celsius,
                     double pressure_kpa,
                     double humidity_percent,
                     bool leak_detected,
                     int signal_quality_percent,
                     Instant timestamp);

    // -- builders --
    HardwareSnapshot shambles();
    HardwareSnapshot average();
    HardwareSnapshot strong();

    // -- accessors --

    [[nodiscard]] int battery_percent() const;
    [[nodiscard]] double temperature_celsius() const;
    [[nodiscard]] double pressure_kpa() const;
    [[nodiscard]] double humidity_percent() const;
    [[nodiscard]] bool leak_detected() const;
    [[nodiscard]] int signal_quality_percent() const;
    [[nodiscard]] Instant timestamp() const;

    // -- mutators (used by simulation loop) --

    void set_battery_percent(int value);
    void set_temperature_celsius(double value);
    void set_pressure_kpa(double value);
    void set_humidity_percent(double value);
    void set_leak_detected(bool value);
    void set_signal_quality_percent(int value);
    void set_timestamp(Instant value);

    // -- serialization --

    friend void to_json(nlohmann::json& j, const HardwareSnapshot& s);
    friend void from_json(const nlohmann::json& j, HardwareSnapshot& s);

private:
    static int clamp_percent(int value);

    int battery_percent_;
    double temperature_celsius_;
    double pressure_kpa_;
    double humidity_percent_;
    bool leak_detected_;
    int signal_quality_percent_;
    Instant timestamp_;
};

} // namespace avisos::model
