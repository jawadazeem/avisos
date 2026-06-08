/**
 * @file HardwareSnapshot.cpp
 * @brief Construction, factory methods, and JSON serialisation for HardwareSnapshot.
 *
 * Factory method value ranges:
 *
 *   | Field       | shambles    | average     | strong       |
 *   |-------------|-------------|-------------|--------------|
 *   | battery     | 1-15%       | 30-70%      | 80-100%      |
 *   | temperature | 55-80°C     | 30-45°C     | 21.5-23°C    |
 *   | pressure    | 80-90 kPa   | 95-105 kPa  | 101.2-101.4  |
 *   | humidity    | 75-95%      | 40-60%      | 44.2-45.5%   |
 *   | signal      | 5-20%       | 40-70%      | 98-99%       |
 *   | leak        | 80% chance  | 20% chance  | never        |
 */

#include "HardwareSnapshot.h"

#include <algorithm>
#include <cmath>
#include <iomanip>
#include <random>
#include <sstream>

namespace avisos::model {

// -- construction --
HardwareSnapshot::HardwareSnapshot()
    : battery_percent_(100),
      temperature_celsius_(22.0),
      pressure_kpa_(101.3),
      humidity_percent_(45.0),
      leak_detected_(false),
      signal_quality_percent_(95),
      timestamp_(Clock::now()) {}

HardwareSnapshot::HardwareSnapshot(int battery_percent,
                                   double temperature_celsius,
                                   double pressure_kpa,
                                   double humidity_percent,
                                   bool leak_detected,
                                   int signal_quality_percent,
                                   Instant timestamp)
    : battery_percent_(clamp_percent(battery_percent)),
      temperature_celsius_(temperature_celsius),
      pressure_kpa_(pressure_kpa),
      humidity_percent_(humidity_percent),
      leak_detected_(leak_detected),
      signal_quality_percent_(clamp_percent(signal_quality_percent)),
      timestamp_(timestamp) {}

// -- builders --
    HardwareSnapshot HardwareSnapshot::shambles() {
        std::mt19937 gen(std::random_device{}());
        std::uniform_int_distribution<int> battery_dist(1, 15);
        int battery = battery_dist(gen);

        std::uniform_real_distribution<double> temp_dist(55.0, 80.0);
        double temp = temp_dist(gen);

        std::uniform_real_distribution<double> pressure_dist(80.0, 90.0);
        double pressure = pressure_dist(gen);

        std::uniform_real_distribution<double> humidity_dist(75.0, 95.0);
        double humidity = humidity_dist(gen);

        std::uniform_int_distribution<int> signal_dist(5, 20);
        int signal = signal_dist(gen);

        // leak almost always detected in shambles state
        std::uniform_int_distribution<int> leak_dist(0, 9);
        bool leak = leak_dist(gen) < 8;

        return HardwareSnapshot(battery, temp, pressure, humidity, leak, signal, Clock::now());
    }

    HardwareSnapshot HardwareSnapshot::average() {
        std::mt19937 gen(std::random_device{}());
        std::uniform_int_distribution<int> battery_dist(30, 70);
        int battery = battery_dist(gen);

        std::uniform_real_distribution<double> temp_dist(30.0, 45.0);
        double temp = temp_dist(gen);

        std::uniform_real_distribution<double> pressure_dist(95.0, 105.0);
        double pressure = pressure_dist(gen);

        std::uniform_real_distribution<double> humidity_dist(40.0, 60.0);
        double humidity = humidity_dist(gen);

        std::uniform_int_distribution<int> signal_dist(40, 70);
        int signal = signal_dist(gen);

        // leak occasionally detected in average state
        std::uniform_int_distribution<int> leak_dist(0, 9);
        bool leak = leak_dist(gen) < 2;

        return HardwareSnapshot(battery, temp, pressure, humidity, leak, signal, Clock::now());
    }
    
    HardwareSnapshot HardwareSnapshot::strong() {
        std::mt19937 gen(std::random_device{}());
        std::uniform_int_distribution<int> battery_dist(80, 100);
        int battery = battery_dist(gen);

        std::uniform_real_distribution<double> temp_dist(21.5, 23.0);
        double temp = temp_dist(gen);
        
        std::uniform_real_distribution<double> pressure_dist(101.2, 101.4);
        double pressure = pressure_dist(gen);
        
        std::uniform_real_distribution<double> humidity_dist(44.2, 45.5);
        double humidity = humidity_dist(gen);

        std::uniform_int_distribution<int> signal_quality_dist(98, 99);
        double signal_quality_percent = signal_quality_dist(gen);
    
        return HardwareSnapshot(battery,
                                temp,
                                pressure,
                                humidity,
                                false,
                                signal_quality_percent,
                                Clock::now()
                );
    }

// -- accessors --

int HardwareSnapshot::battery_percent() const { return battery_percent_; }
double HardwareSnapshot::temperature_celsius() const { return temperature_celsius_; }
double HardwareSnapshot::pressure_kpa() const { return pressure_kpa_; }
double HardwareSnapshot::humidity_percent() const { return humidity_percent_; }
bool HardwareSnapshot::leak_detected() const { return leak_detected_; }
int HardwareSnapshot::signal_quality_percent() const { return signal_quality_percent_; }
HardwareSnapshot::Instant HardwareSnapshot::timestamp() const { return timestamp_; }

// -- mutators --

void HardwareSnapshot::set_battery_percent(int value) { battery_percent_ = clamp_percent(value); }
void HardwareSnapshot::set_temperature_celsius(double value) { temperature_celsius_ = value; }
void HardwareSnapshot::set_pressure_kpa(double value) { pressure_kpa_ = value; }
void HardwareSnapshot::set_humidity_percent(double value) { humidity_percent_ = value; }
void HardwareSnapshot::set_leak_detected(bool value) { leak_detected_ = value; }
void HardwareSnapshot::set_signal_quality_percent(int value) { signal_quality_percent_ = clamp_percent(value); }
void HardwareSnapshot::set_timestamp(Instant value) { timestamp_ = value; }

// -- validation --

int HardwareSnapshot::clamp_percent(int value) {
    return std::clamp(value, 0, 100);
}

// -- serialization --
// Field names match the Java HardwareSnapshot record:
//   batteryPercent, temperatureCelsius, pressureKpa,
//   humidityPercent, leakDetected, signalQualityPercent, timestamp

void to_json(nlohmann::json& j, const HardwareSnapshot& s) {
    // Format timestamp as ISO-8601 UTC (e.g. "2026-06-08T12:30:00Z")
    auto time_t = HardwareSnapshot::Clock::to_time_t(s.timestamp_);
    std::ostringstream oss;
    oss << std::put_time(std::gmtime(&time_t), "%Y-%m-%dT%H:%M:%SZ");

    j = nlohmann::json{
        {"batteryPercent",        s.battery_percent_},
        {"temperatureCelsius",    s.temperature_celsius_},
        {"pressureKpa",           s.pressure_kpa_},
        {"humidityPercent",       s.humidity_percent_},
        {"leakDetected",          s.leak_detected_},
        {"signalQualityPercent",  s.signal_quality_percent_},
        {"timestamp",             oss.str()}
    };
}

void from_json(const nlohmann::json& j, HardwareSnapshot& s) {
    j.at("batteryPercent").get_to(s.battery_percent_);
    j.at("temperatureCelsius").get_to(s.temperature_celsius_);
    j.at("pressureKpa").get_to(s.pressure_kpa_);
    j.at("humidityPercent").get_to(s.humidity_percent_);
    j.at("leakDetected").get_to(s.leak_detected_);
    j.at("signalQualityPercent").get_to(s.signal_quality_percent_);

    // Parse ISO-8601 timestamp string back to time_point
    std::string ts = j.at("timestamp").get<std::string>();
    std::tm tm = {};
    std::istringstream iss(ts);
    iss >> std::get_time(&tm, "%Y-%m-%dT%H:%M:%SZ");
    s.timestamp_ = HardwareSnapshot::Clock::from_time_t(std::mktime(&tm));

    s.battery_percent_ = clamp_percent(s.battery_percent_);
    s.signal_quality_percent_ = clamp_percent(s.signal_quality_percent_);
}

} // namespace avisos::model
