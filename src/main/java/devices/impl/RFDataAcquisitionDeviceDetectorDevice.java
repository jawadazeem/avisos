/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package devices.impl;

import core.SecurityHub;
import devices.api.DataAcquisitionDevice;
import devices.api.DataAcquisitionDeviceDetectorDevice;
import devices.api.RFHardwareLink;

import java.util.List;
import java.util.UUID;

public class RFDataAcquisitionDeviceDetectorDevice implements DataAcquisitionDeviceDetectorDevice {
    private final SecurityHub hub;
    private final RFHardwareLink hardwareLink;

    public RFDataAcquisitionDeviceDetectorDevice(SecurityHub hub, RFHardwareLink hardwareLink) {
        this.hub = hub;
        this.hardwareLink = hardwareLink;
    }

    public void pair(UUID Id) {
        if (detectIfNew(Id)) {
            hardwareLink.pair(Id);
        }
    }

    public boolean detectIfNew(UUID Id) {
        List<DataAcquisitionDevice> devices = hub.getDataAcquisitionDevices();
        if (devices.stream().anyMatch(d -> d.getId().equals(Id))) {
            return false;
        }
        return true;
    }
}
