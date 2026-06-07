/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.config;

import java.time.Duration;

/**
 * Hardware telemetry source configuration.
 *
 * @param provider provider mode, either {@code local} or {@code simulator-rest}
 * @param simulatorBaseUrl base URL for the C++ hardware simulator
 * @param requestTimeout timeout for simulator HTTP requests
 */
public record HardwareConfig(String provider, String simulatorBaseUrl, Duration requestTimeout) {}
