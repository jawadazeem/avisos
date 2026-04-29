/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.factories;

import com.azeem.avisos.node.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.node.devices.api.HardwareLink;
import com.azeem.avisos.node.devices.impl.GlassBreakSensorDataAcquisitionDevice;
import com.azeem.avisos.infrastructure.logger.Logger;

import java.util.UUID;

public class GlassBreakDeviceFactory implements DeviceFactory {

    @Override
    public DataAcquisitionDevice create(UUID Id, Logger logger, HardwareLink hardwareLink) {
        return new GlassBreakSensorDataAcquisitionDevice(Id, logger, hardwareLink);
    }

    @Override
    public DataAcquisitionDevice create(Logger logger, HardwareLink hardwareLink) {
        return new GlassBreakSensorDataAcquisitionDevice(UUID.randomUUID(), logger, hardwareLink);
    }
}
