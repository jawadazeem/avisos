/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.health;

import com.azeem.avisos.controller.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class SystemHealthMonitor {

    private static final Logger log = LoggerFactory.getLogger(SystemHealthMonitor.class);

    private volatile SystemHealthReport latestReport;

    private final DatabaseHealthCheck dbHealthCheck;
    private final ExecutorService executor;

    // thresholds
    private static final long DISK_THRESHOLD_BYTES = 100L * 1024 * 1024; // 100MB
    private static final long DB_TIMEOUT_MS = 500;

    public SystemHealthMonitor(DatabaseHealthCheck dbHealthCheck, ExecutorService executor) {
        this.dbHealthCheck = dbHealthCheck;
        this.executor = executor;
    }

    /**
     * Public API
     */
    public SystemHealthReport checkSystemHealth() {

        List<ComponentHealth> results = new ArrayList<>();

        results.add(checkDatabase());
        results.add(checkStorage());

        HealthStatusLevel overall = computeOverall(results);

        return new SystemHealthReport(overall, results);
    }

    public void refreshHealth() {
        latestReport = checkSystemHealth();
    }

    private ComponentHealth checkDatabase() {
        long start = System.currentTimeMillis();

        Future<Boolean> future = executor.submit(dbHealthCheck::check);

        try {
            future.get(DB_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            return new ComponentHealth(
                    "database",
                    HealthStatusLevel.HEALTHY,
                    "DB responding normally",
                    System.currentTimeMillis() - start
            );

        } catch (TimeoutException e) {

            log.error("Database health check TIMEOUT");

            return new ComponentHealth(
                    "database",
                    HealthStatusLevel.UNHEALTHY,
                    "DB timeout exceeded " + DB_TIMEOUT_MS + "ms",
                    System.currentTimeMillis() - start
            );

        } catch (Exception e) {

            log.error("Database health check FAILED", e);

            return new ComponentHealth(
                    "database",
                    HealthStatusLevel.UNHEALTHY,
                    "DB error: " + e.getMessage(),
                    System.currentTimeMillis() - start
            );
        }
    }

    private ComponentHealth checkStorage() {
        long start = System.currentTimeMillis();

        try {
            File root = new File("/"); // system mount point

            long usable = root.getUsableSpace();

            if (usable < DISK_THRESHOLD_BYTES) {
                return new ComponentHealth(
                        "storage",
                        HealthStatusLevel.DEGRADED,
                        "Low disk space: " + usable + " bytes",
                        System.currentTimeMillis() - start
                );
            }

            return new ComponentHealth(
                    "storage",
                    HealthStatusLevel.HEALTHY,
                    "Disk space OK",
                    System.currentTimeMillis() - start
            );

        } catch (Exception e) {

            log.error("Storage health check FAILED", e);

            return new ComponentHealth(
                    "storage",
                    HealthStatusLevel.UNHEALTHY,
                    "Storage error: " + e.getMessage(),
                    System.currentTimeMillis() - start
            );
        }
    }

    private HealthStatusLevel computeOverall(List<ComponentHealth> components) {

        boolean hasUnhealthy = components.stream()
                .anyMatch(c -> c.status() == HealthStatusLevel.UNHEALTHY);

        if (hasUnhealthy) return HealthStatusLevel.UNHEALTHY;

        boolean hasDegraded = components.stream()
                .anyMatch(c -> c.status() == HealthStatusLevel.DEGRADED);

        if (hasDegraded) return HealthStatusLevel.DEGRADED;

        return HealthStatusLevel.HEALTHY;
    }
}
