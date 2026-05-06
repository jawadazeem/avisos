/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.common.domain;

import java.time.Instant;
import java.util.UUID;

public record TelemetryPacket(
        UUID deviceId,
        PacketTypeMapper type,
        byte[] payload, // Could be a JPEG frame or a JSON string
        Instant timestamp
) {}
