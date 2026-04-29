/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package devices.api;

import devices.model.DeviceStatus;

public interface DataAcquisitionDevice extends Identifiable, Pingable, BatteryMonitored, Recoverable, Observable, Connectable {

    void performSelfCheck();
    void setDeviceStatus(DeviceStatus deviceStatus);
    DeviceStatus getDeviceStatus();
}
