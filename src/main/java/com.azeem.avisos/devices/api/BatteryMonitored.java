/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.devices.api;

public interface BatteryMonitored {
    int getBatteryLife();
    boolean isBatteryFull();
    boolean isBatteryEmpty();
    void setBatteryLife(int batteryLife);
}
