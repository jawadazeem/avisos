/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.node;

import java.time.LocalDateTime;
import java.util.UUID;

public record NodeRecord(
        UUID uuid,
        String name,
        String type,
        NodeStatus status,
        double batteryLevel,
        LocalDateTime lastSeen
) {}
