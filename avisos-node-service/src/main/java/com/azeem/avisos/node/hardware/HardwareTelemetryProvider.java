/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.hardware;

/** Supplies the latest hardware vitals for node telemetry and watchdog checks. */
public interface HardwareTelemetryProvider {
  HardwareSnapshot readSnapshot();
}
