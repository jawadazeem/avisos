/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package infrastructure.factories;

import devices.api.DataAcquisitionDevice;
import devices.api.HardwareLink;
import devices.impl.SmokeDataAcquisitionDevice;
import infrastructure.logger.Logger;

import java.util.UUID;

public class SmokeDeviceFactory implements DeviceFactory {

    @Override
    public DataAcquisitionDevice create(UUID Id, Logger logger, HardwareLink hardwareLink) {
        return new SmokeDataAcquisitionDevice(Id, logger, hardwareLink);
    }

    @Override
    public DataAcquisitionDevice create(Logger logger, HardwareLink hardwareLink) {
        return new SmokeDataAcquisitionDevice(UUID.randomUUID(), logger, hardwareLink);
    }
}
