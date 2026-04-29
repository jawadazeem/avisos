/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.devices.impl;

import com.azeem.avisos.core.SecurityHub;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.devices.api.DataAcquisitionDeviceDetectorDevice;
import com.azeem.avisos.devices.api.RFHardwareLink;

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
