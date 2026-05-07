/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.entity.node;

import java.time.LocalDateTime;

public record NodeEntity(
        String uuid,
        String name,
        String type,
        String status,
        double batteryLevel,
        LocalDateTime lastSeen
) {}