/**
 * @file HardwareSnapshot.h
 * @brief Telemetry data model mirroring the Java HardwareSnapshot record.
 *
 * This is the wire-format contract between the C++ simulator and the Java
 * node service. The JSON keys produced by to_json() use camelCase to match
 * the Java record field names exactly (batteryPercent, temperatureCelsius, etc.)
 * so Jackson deserialises them without a custom mapper.
 *
 * Three static factory methods (shambles / average / strong) generate
 * randomised snapshots representing different hardware health tiers.
 */

#pragma once

#include <chrono>
#include <string>

#include <nlohmann/json.hpp>

namespace avisos::model {

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

    // -- factory methods (randomised within tier ranges) --

    /** Critical state: low battery, overheating, high humidity, likely leaking. */
    static HardwareSnapshot shambles();
    /** Normal operating state: moderate readings across all sensors. */
    static HardwareSnapshot average();
    /** Optimal state: full battery, cool, stable pressure, no leaks. */
    static HardwareSnapshot strong();

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

    // -- serialization (nlohmann ADL pattern) --
    // Friends have access to private fields for direct serialisation.
    // JSON keys are camelCase to match the Java HardwareSnapshot record.

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
