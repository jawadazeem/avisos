/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.devices.api;

import com.azeem.avisos.node.devices.model.DeviceStatus;

public interface DataAcquisitionDevice extends Identifiable, Pingable, BatteryMonitored, Recoverable, Observable, Connectable {

    void performSelfCheck();
    void setDeviceStatus(DeviceStatus deviceStatus);
    DeviceStatus getDeviceStatus();
}
