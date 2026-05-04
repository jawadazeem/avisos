/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.mapper.device;

import com.azeem.avisos.controller.entity.device.DeviceEntity;
import com.azeem.avisos.controller.model.device.DeviceRecord;
import com.azeem.avisos.controller.model.device.DeviceStatus;

import java.util.UUID;

public class DeviceMapper {
    public static DeviceRecord toDomain(DeviceEntity entity) {
        return new DeviceRecord(
                UUID.fromString(entity.uuid()),
                entity.name(),
                entity.type(),
                DeviceStatus.valueOf(entity.status()),
                entity.batteryLevel(),
                entity.lastSeen()
        );
    }

    public static DeviceEntity toEntity(DeviceRecord record) {
        return new DeviceEntity(
                record.uuid().toString(),
                record.name(),
                record.type(),
                record.status().name(),
                record.batteryLevel(),
                record.lastSeen()
        );
    }
}
