/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.device.model.telemetry;

import java.time.Instant;
import java.util.UUID;

/**
 * A Data Transfer Object (DTO) representing a structured telemetry report to send to the Controller
 * <p>
 * This record is the primary domain model for device status and sensor data within
 * the Avisos ecosystem. It is typically serialized into JSON via Jackson or
 * mapped from a Protobuf binary format.
 * </p>
 * @param deviceId    The unique identifier (UUID) of this physical node
 * @param type        The functional category of the packet (e.g., HEARTBEAT, NETWORK_SCAN).
 * @param payload The raw data payload (e.g., image bytes or sensor readings) intended for
 * further analysis
 * @param timestamp   The moment the telemetry was generated on the Node side.
 */
public record TelemetryPacketDto(
        UUID deviceId,
        PacketTypeDto type,
        byte[] payload,
        Instant timestamp
) {}
