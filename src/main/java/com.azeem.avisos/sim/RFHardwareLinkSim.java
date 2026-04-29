/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.sim;

import com.azeem.avisos.core.SecurityHub;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.devices.api.RFHardwareLink;
import com.azeem.avisos.devices.impl.GlassBreakSensorDataAcquisitionDevice;
import com.azeem.avisos.devices.impl.MotionDataAcquisitionDevice;
import com.azeem.avisos.devices.impl.SmokeDataAcquisitionDevice;
import com.azeem.avisos.devices.impl.ThermalDataAcquisitionDevice;
import com.azeem.avisos.devices.model.DeviceStatus;
import com.azeem.avisos.infrastructure.logger.Logger;

import java.util.UUID;

/**
 * <h3>Hardware Link Simulation Used to Connect to Data Acquisition Devices (DADs)</h3>
 * Simulates a real connection to a hardware link connected via Radio Frequency
 */
public class RFHardwareLinkSim implements RFHardwareLink {
    private final SecurityHub hub;
    private final Logger logger;

    public RFHardwareLinkSim(SecurityHub hub, Logger logger) {
        this.hub = hub;
        this.logger = logger;
    }

    /**
     * Hub pairs with randomly generated device for a given UUID
     */
    @Override
    public void pair(UUID Id) {
        hub.addDevice(createDataAcquisitionDevice(Id));
    }

    @Override
    public boolean isDeviceResponsive(DataAcquisitionDevice dataAcquisitionDevice) {
        if (hub.getDataAcquisitionDevices().contains(dataAcquisitionDevice)
                && dataAcquisitionDevice.getBatteryLife() > 0
                && dataAcquisitionDevice.getSignalStrength() >= -100
                && dataAcquisitionDevice.getDeviceStatus() == DeviceStatus.OPERATIONAL) {
            return Math.random() > 0.05;
        }
        return false;
    }

    /**
     * Randomly creates a new Data Acquisition Device (DAD). In a real system this method would pair with a real, already existing, tangible, device.
     */

    private DataAcquisitionDevice createDataAcquisitionDevice(UUID Id) {
        double rand = Math.random();

        if (rand < 0.3) {
            return new MotionDataAcquisitionDevice(Id, logger, this);
        } else if (rand < 0.6) {
            return new SmokeDataAcquisitionDevice(Id, logger, this);
        } else if (rand < 0.8) {
            return new ThermalDataAcquisitionDevice(Id, logger, this);
        } else {
            return new GlassBreakSensorDataAcquisitionDevice(Id, logger, this);
        }
    }
}
