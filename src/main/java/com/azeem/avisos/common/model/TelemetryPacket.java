/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.common.model;

import java.time.Instant;
import java.util.UUID;

/**
 * The ONLY thing that moves from Data Acquisition Device (DAD) to the Controller.
 */
public record TelemetryPacket(
        UUID deviceId,
        PacketType type,
        byte[] payload, // Could be a JPEG frame or a JSON string
        Instant timestamp
) {}
