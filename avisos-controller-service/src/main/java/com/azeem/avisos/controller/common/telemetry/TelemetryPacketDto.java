/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.common.telemetry;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable telemetry event received from a remote node device.
 *
 * <p>
 * This record represents the canonical telemetry format used by the
 * Controller service. It is deserialized from JSON messages received
 * over MQTT and used as the input for downstream processing such as
 * vision analysis, monitoring, and alert generation.
 * </p>
 *
 * <p>
 * The payload field may contain binary data (e.g., images), which is
 * transmitted as Base64-encoded data in JSON and automatically decoded
 * by Jackson during deserialization.
 * </p>
 *
 * @param deviceId      unique identifier of the node that sent this telemetry
 * @param batteryLevel  current battery percentage reported by the node
 * @param deviceName    human-readable name of the sending node
 * @param type          telemetry packet category (e.g., HEARTBEAT, NETWORK_SCAN)
 * @param payload       raw sensor or image data sent from the node
 * @param timestamp     UTC timestamp indicating when the telemetry was generated on the node
 */
public record TelemetryPacketDto(
        UUID deviceId,
        int batteryLevel,
        String deviceName,
        PacketTypeDto type,
        byte[] payload,
        Instant timestamp
) {}
