/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.device;

import com.azeem.avisos.controller.model.device.DeviceRecord;

import java.util.List;
import java.util.UUID;

public interface DeviceService {
    void updateDeviceHeartbeat(DeviceRecord deviceRecord);
    void checkStaleDevices();
    List<UUID> getRegisteredDevices();
    void registerHeartbeat(UUID uuid);
}
