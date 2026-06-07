/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.hardware;

import java.time.Instant;

/**
 * Point-in-time hardware vitals reported by either local host hardware or the C++ simulator.
 *
 * @param batteryPercent current battery percentage
 * @param temperatureCelsius internal temperature in Celsius
 * @param pressureKpa pressure in kilopascals
 * @param leakDetected whether water ingress has been detected
 * @param humidityPercent internal humidity percentage
 * @param signalQualityPercent communications signal quality percentage
 * @param timestamp time when the reading was produced
 */
public record HardwareSnapshot(
    int batteryPercent,
    Double temperatureCelsius,
    Double pressureKpa,
    Boolean leakDetected,
    Double humidityPercent,
    Integer signalQualityPercent,
    Instant timestamp) {

  public HardwareSnapshot {
    batteryPercent = clampBattery(batteryPercent);
    timestamp = timestamp == null ? Instant.now() : timestamp;
  }

  public static HardwareSnapshot localBattery(int batteryPercent) {
    return new HardwareSnapshot(batteryPercent, null, null, null, null, null, Instant.now());
  }

  private static int clampBattery(int value) {
    if (value < 0) {
      return 0;
    }
    return Math.min(value, 100);
  }
}
