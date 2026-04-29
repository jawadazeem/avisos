/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.infrastructure.factories;


import com.azeem.avisos.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.devices.api.HardwareLink;
import com.azeem.avisos.infrastructure.logger.Logger;

import java.util.UUID;

public interface DeviceFactory {

    DataAcquisitionDevice create(UUID Id, Logger logger, HardwareLink hardwareLink);
    DataAcquisitionDevice create(Logger logger, HardwareLink hardwareLink);
}
