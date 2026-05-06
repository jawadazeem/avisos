/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.device;

import com.azeem.avisos.controller.model.device.DeviceRecord;
import com.azeem.avisos.controller.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class SimpleDeviceService implements DeviceService {
    private static final Logger log = LoggerFactory.getLogger(SimpleDeviceService.class);
    DeviceRepository deviceRepository;

    public SimpleDeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public void updateDeviceHeartbeat(DeviceRecord deviceRecord) {
        deviceRepository.updateDeviceLastSeen(deviceRecord.uuid().toString());
    }

    @Override
    public void checkStaleDevices() {
        int offlineCount = deviceRepository.markStaleDevicesOffline(60); // 60 second threshold
        if (offlineCount > 0) {
            log.warn("Marked {} devices as OFFLINE due to heartbeat timeout.", offlineCount);
        }
    }

    @Override
    public List<UUID> getRegisteredDevices() {
        List<String> strings  = deviceRepository.getRegisteredDeviceUuids();
        return strings.stream().map(UUID::fromString).toList();
    }

    @Override
    public void registerHeartbeat(UUID uuid) {
        deviceRepository.updateDeviceLastSeen(uuid.toString());
    }
}
