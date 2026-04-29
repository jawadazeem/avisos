/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.devices.api;


/**
 * Hardware link used for Data Acquisition Devices (DADs). Uses direct fiber optic connection.
 */
public interface DirectConnectDXHardwareLink extends HardwareLink {
    void enableLink(int Id);
}
