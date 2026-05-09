/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.health;

import java.util.List;

public record SystemHealthReport(
        HealthStatusLevel overallStatus,
        List<ComponentHealth> components
) {}
