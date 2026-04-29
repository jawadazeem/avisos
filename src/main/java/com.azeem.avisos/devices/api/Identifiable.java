/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.devices.api;

import com.azeem.avisos.devices.model.DeviceType;

import java.util.UUID;

public interface Identifiable {
    UUID getId();
    DeviceType getDeviceType();
}
