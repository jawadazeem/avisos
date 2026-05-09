/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.hardware;

import com.azeem.avisos.node.service.HeartbeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PowerSource;

import java.util.List;

/**
 * Provides access to system battery information using OSHI.
 *
 * <p>
 * This class exposes hardware-level battery telemetry
 * for the local node device.
 * </p>
 */
public class BatteryProvider {
    private static final Logger log = LoggerFactory.getLogger(BatteryProvider.class);
    private static final int ASSUMED_LINE_POWER_LEVEL = 100;

    /**
     * <p>
     * The returned value represents the remaining battery
     * percentage of the primary detected power source.
     * </p>
     *
     * @return the current battery level percentage
     */
    public int getBatteryLevel() {
        List<PowerSource> powerSources = getPowerSources();

        if (powerSources.isEmpty()) {
            log.debug(
                    "No battery power source detected; assuming line power at {}%",
                    ASSUMED_LINE_POWER_LEVEL
            );
            return ASSUMED_LINE_POWER_LEVEL;
        }

        return powerSources.getFirst().getCurrentCapacity();
    }

    /**
     * Retrieves all detected system power sources.
     *
     * @return a list of available system power sources
     */
    private List<PowerSource> getPowerSources() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        return hal.getPowerSources();
    }
}
