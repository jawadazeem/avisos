/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package devices.impl;

import devices.api.DataAcquisitionDevice;
import devices.api.HardwareLink;
import devices.model.DeviceStatus;
import infrastructure.logger.LogLevel;
import infrastructure.logger.Logger;

import java.util.UUID;

public abstract class BaseDataAcquisitionDevice implements DataAcquisitionDevice {
    protected int failureCount = 0;
    protected DeviceStatus deviceStatus = DeviceStatus.AWAY; // DataAcquisitionDevice is away by default
    protected HardwareLink hardwareLink;
    protected Logger logger;
    protected final UUID Id;

    public BaseDataAcquisitionDevice(UUID Id, Logger logger, HardwareLink hardwareLink) {
        this.Id = Id;
        this.logger = logger;
        this.hardwareLink = hardwareLink;
    }

    @Override
    public void resetFailureCount() {
        failureCount = 0;
    }

    @Override
    public void incrementFailureCount() {
        failureCount++;
    }

    @Override
    public int getFailureCount() {
        return failureCount;
    }

    @Override
    public DeviceStatus getDeviceStatus() {
        return deviceStatus;
    }

    @Override
    public void setDeviceStatus(DeviceStatus deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    @Override
    public boolean ping() {
        if (hardwareLink.isDeviceResponsive(this)) {
            logger.log("Motion DataAcquisitionDevice (" + this.getId() + ") was successfully pinged", LogLevel.INFO);
            return true;
        } else {
            logger.log("Could not ping Motion DataAcquisitionDevice (" + this.getId() + ") successfully", LogLevel.ERROR);
            return false;
        }
    }
}
