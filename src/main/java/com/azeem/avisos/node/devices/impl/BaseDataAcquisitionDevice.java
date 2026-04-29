/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.devices.impl;

import com.azeem.avisos.node.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.node.devices.api.HardwareLink;
import com.azeem.avisos.node.devices.model.DeviceStatus;
import com.azeem.avisos.infrastructure.logger.LogLevel;
import com.azeem.avisos.infrastructure.logger.Logger;

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
