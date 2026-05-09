/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.health;

public record ComponentHealth(
        String component,
        HealthStatusLevel status,
        String message,
        long latencyMs
) {}
