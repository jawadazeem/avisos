/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.devices.api;

public interface Connectable {
    int getSignalStrength();
    void setSignalStrength(int signalStrength);
}
