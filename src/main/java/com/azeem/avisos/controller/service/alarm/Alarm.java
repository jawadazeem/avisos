/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.alarm;

import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import com.azeem.avisos.controller.model.alarm.AlarmStatus;
import com.azeem.avisos.node.devices.model.DeviceType;

import java.time.LocalDateTime;
import java.util.UUID;

public class Alarm {
    private final UUID Id;
    private final UUID deviceId;
    private final DeviceType deviceType;
    private final LocalDateTime timestamp;
    private final AlarmSeverity severity;
    private AlarmStatus status;

    public Alarm(UUID Id,
                 DeviceType deviceType,
                 UUID deviceId,
                 AlarmStatus status,
                 AlarmSeverity severity,
                 LocalDateTime timestamp
    ) {
        this.Id = Id;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.severity = severity;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Alarm(DeviceType deviceType,
                 UUID deviceId,
                 AlarmStatus status,
                 AlarmSeverity severity,
                 LocalDateTime timestamp
    ) {
        this.Id = UUID.randomUUID();
        this.deviceType = deviceType;
        this.deviceId = deviceId;
        this.severity = severity;
        this.status = status;
        this.timestamp = timestamp;
    }

    public void resolveAlarm() {
        if (status == AlarmStatus.ACTIVE) {
            status = AlarmStatus.RESOLVED;
        } else {
            System.err.println("Cannot resolve an already resolved com.azeem.avisos.alarm.");
        }
    }

    public UUID getId() {
        return Id;
    }

    public UUID getDeviceId() {
        return deviceId;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public AlarmSeverity getSeverity() {
        return severity;
    }
}
