/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.devices.api;

import java.util.UUID;

/**
 * Hardware link used for Data Acquisition Devices (DADs). Uses radio frequencies.
 */
public interface RFHardwareLink extends HardwareLink {
    void pair(UUID Id);
}
