/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.node;

import java.time.Instant;

public record FleetMetricRecord(
    Instant timestamp,
    FleetMetrics fleetMetrics
) {
}
