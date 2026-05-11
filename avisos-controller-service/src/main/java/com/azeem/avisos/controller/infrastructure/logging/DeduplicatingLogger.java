/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.logging;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * High-velocity edge noise suppression tool for logging.
 * Collapses repetitive error signatures into a single summary log.
 */
public class DeduplicatingLogger {
    private static final Logger log = LoggerFactory.getLogger(DeduplicatingLogger.class);

    // Map of NodeID + ErrorCode -> Occurrence Count
    private final Map<String, AtomicInteger> errorCache = new ConcurrentHashMap<>();
    private final String componentName;

    public DeduplicatingLogger(String componentName) {
        this.componentName = componentName;
    }

    public void logError(UUID nodeId, String errorCode, String message) {
        String key = nodeId.toString() + ":" + errorCode;

        errorCache.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();

        // Only log to disk/console every 100th occurrence to save IOPS
        int count = errorCache.get(key).get();
        if (count == 1) {
            log.error("[{}] FIRST OCCURRENCE - Node: {}, Code: {}, Msg: {}",
                    componentName, nodeId, errorCode, message);
        } else if (count % 100 == 0) {
            log.warn("[{}] REPETITIVE ERROR - Node: {}, Code: {}, Count: {}",
                    componentName, nodeId, errorCode, count);
        }
    }

    public void flush() {
        errorCache.clear();
        log.info("[{}] Error cache cleared.", componentName);
    }
}
