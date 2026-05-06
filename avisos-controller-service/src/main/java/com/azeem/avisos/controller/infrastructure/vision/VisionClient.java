/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.vision;

import com.azeem.avisos.controller.model.vision.VisionRequest;
import com.azeem.avisos.controller.model.vision.VisionResponse;

/**
 * Core Service Contract for Computer Vision Orchestration.
 * Designed for asynchronous, non-blocking execution at the edge.
 */
public interface VisionClient {
    /**
     * Standard synchronous-style call (blocking the virtual thread, not the platform).
     * Used for real-time telemetry processing.
     */
    VisionResponse sendRequest(VisionRequest request);

    /**
     * Health check to support Circuit Breaker state transitions.
     * Essential for "Operational Excellence" and monitoring.
     */
    boolean isAvailable();
}
