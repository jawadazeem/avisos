/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.common.mapper;

import com.azeem.avisos.common.proto.PacketType;
import com.azeem.avisos.controller.common.telemetry.PacketTypeDto;
import com.azeem.avisos.controller.common.telemetry.TelemetryPacketDto;
import com.azeem.avisos.common.proto.TelemetryPacket;

import java.time.Instant;
import java.util.UUID;

public class TelemetryPacketMapper {
    public TelemetryPacketDto mapToDomain(TelemetryPacket packet) {
        return new TelemetryPacketDto(
                UUID.fromString(packet.getDeviceId()),
                packet.getBatteryLevel(),
                packet.getDeviceName(),
                mapTypeToDomain(packet.getType()),
                packet.getPayload().toByteArray(),
                Instant.ofEpochMilli(packet.getTimestamp())
        );
    }

    private PacketTypeDto mapTypeToDomain(PacketType type) {
        return switch (type) {
            case HEARTBEAT -> PacketTypeDto.HEARTBEAT;
            case NETWORK_SCAN -> PacketTypeDto.NETWORK_SCAN;
            case SYSTEM_ERROR -> PacketTypeDto.SYSTEM_ERROR;
            default -> PacketTypeDto.UNRECOGNIZED;
        };
    }
}
