/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

public record VisionConfig(
        String apiUrl,
        double minConfidence,
        int timeoutSeconds
) {}
