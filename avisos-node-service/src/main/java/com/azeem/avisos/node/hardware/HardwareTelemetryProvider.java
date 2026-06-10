/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.hardware;

/** Supplies the latest hardware vitals for node telemetry and watchdog checks. */
public interface HardwareTelemetryProvider {
  HardwareSnapshot readSnapshot();

  /**
   * Returns a camera frame from the hardware source. Simulator-backed providers fetch from the C++
   * simulator's /frame endpoint; local providers return an empty array.
   */
  default byte[] readFrame() {
    return new byte[0];
  }
}
