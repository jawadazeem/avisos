/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.device;

import com.azeem.avisos.controller.model.device.DeviceRecord;
import com.azeem.avisos.controller.repository.DeviceRepository;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class DeviceService {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(DeviceService.class);
    DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public void updateDeviceHeartbeat(DeviceRecord deviceRecord) {
        deviceRepository.updateDeviceLastSeen(deviceRecord.uuid());
    }

    public void checkStaleDevices() {
        int offlineCount = deviceRepository.markStaleDevicesOffline(60); // 60 second threshold
        if (offlineCount > 0) {
            log.warn("Marked {} devices as OFFLINE due to heartbeat timeout.", offlineCount);
        }
    }

    public List<UUID> getRegisteredDevices() {
        return deviceRepository.getRegisteredDevices();
    }
}
