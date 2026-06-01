/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds vision API configuration from {@code avisos.vision.*} in application.yml. */
@ConfigurationProperties(prefix = "avisos.vision")
public record AvisosVisionProperties(String apiUrl, double minConfidence, int timeoutSeconds) {}
