/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.infrastructure.logger;

public enum LogLevel {
    INFO,     // General system updates
    HEALTH,   // Battery/Signal heartbeats
    WARNING,  // Potential issues (Battery < 10%)
    ALARM,    // Actual security triggers
    CRITICAL,  // System failure or Panic
    ERROR // An operation could not take place despite being requested
}
