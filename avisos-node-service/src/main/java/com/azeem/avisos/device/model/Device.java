/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.device.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the node/device this program is running on.
 */
public record Device(
        UUID uuid,
        String name,
        String type,
        DeviceStatus status,
        double batteryLevel,
        LocalDateTime lastSeen
) {}

