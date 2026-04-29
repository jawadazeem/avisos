/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package devices.api;

import devices.model.DeviceType;

import java.util.UUID;

public interface Identifiable {
    UUID getId();
    DeviceType getDeviceType();
}
