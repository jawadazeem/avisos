/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.device;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeviceRecord(
        UUID uuid,
        String name,
        String type,
        String status,
        double batteryLevel,
        LocalDateTime lastSeen
) {}
