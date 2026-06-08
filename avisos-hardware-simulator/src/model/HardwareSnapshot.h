#pragma once
#include <string>
#include <chrono>

class HardwareSnapshot {
public:
    using Instant = std::chrono::time_point<std::chrono::high_resolution_clock>;

private:
    int batteryPercent;
    double temperatureCelsius;
    double pressureKpa;
    double humidityPercent;
    bool leakDetected;
    int signalQualityPercent;
    Instant timestamp;

public:
    HardwareSnapshot(int batteryPercent,
                    double temperatureCelsius,
                    double pressureKpa,
                    double humidityPercent,
                    bool leakDetected,
                    int signalQualityPercent,
                    Instant timestamp
    );

    int getBatteryPercent() const;
    double getTemperatureCelsius() const;
    double getPressureKpa() const;
    double getHumidityPercent() const;
    bool isLeakDetected() const;
    int getSignalQualityPercent() const;
    Instant getTimestamp() const;

    void setBatteryPercent(int percent);
    void setTemperatureCelsius(double temp);
    void setPressureKpa(double pressure);
    void setHumidityPercent(double humidity);
    void setLeakDetected(bool leaked);
    void setSignalQualityPercent(int quality);
    void setTimestamp(Instant time);
}