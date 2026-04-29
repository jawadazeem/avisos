/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package infrastructure.factories;


import devices.api.DataAcquisitionDevice;
import devices.api.HardwareLink;
import infrastructure.logger.Logger;

import java.util.UUID;

public interface DeviceFactory {

    DataAcquisitionDevice create(UUID Id, Logger logger, HardwareLink hardwareLink);
    DataAcquisitionDevice create(Logger logger, HardwareLink hardwareLink);
}
