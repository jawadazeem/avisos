/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.entity.node;

import java.time.LocalDateTime;

/**
 * Immutable state representation of a Node.
 */
public record NodeEntity(
        String uuid,
        String name,
        String type,
        String status,
        double batteryLevel,
        LocalDateTime lastSeen
) {
    /**
     * Functional update method to create a new state snapshot.
     */
    public NodeEntity withHeartbeat(double newBattery, String newStatus) {
        return new NodeEntity(
                this.uuid,
                this.name,
                this.type,
                newStatus,
                newBattery,
                LocalDateTime.now()
        );
    }
}