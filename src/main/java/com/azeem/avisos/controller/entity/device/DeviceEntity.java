/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.entity.device;

import java.time.LocalDateTime;

public record DeviceEntity(
        String uuid,
        String name,
        String type,
        String status,
        double batteryLevel,
        LocalDateTime lastSeen
) {}