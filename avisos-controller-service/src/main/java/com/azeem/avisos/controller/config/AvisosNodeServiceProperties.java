/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds node service configuration from {@code avisos.node-service.*} in application.yml. */
@ConfigurationProperties(prefix = "avisos.node-service")
public record AvisosNodeServiceProperties(int staleThreshold, int minHeartbeatIntervalMs) {}
