/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * System health monitoring with component-level checks.
 *
 * <p>{@link com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor} runs periodic
 * checks (every 10s via scheduled executor) and produces a {@link
 * com.azeem.avisos.controller.infrastructure.health.SystemHealthReport} with per-component status:
 *
 * <ul>
 *   <li><b>Database</b> &mdash; executes {@code SELECT 1} with a 500ms timeout to verify
 *       connectivity and measure latency.
 *   <li><b>Disk space</b> &mdash; flags DEGRADED when usable space drops below 100 MB.
 * </ul>
 *
 * <p>Overall status is computed as: any UNHEALTHY component makes the whole system UNHEALTHY,
 * otherwise any DEGRADED makes it DEGRADED, otherwise HEALTHY.
 */
package com.azeem.avisos.controller.infrastructure.health;
