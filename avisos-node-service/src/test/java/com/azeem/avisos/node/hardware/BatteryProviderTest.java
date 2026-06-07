/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BatteryProviderTest {
  @Test
  void readSnapshot_shouldExposeLocalBatteryLevel() {
    BatteryProvider provider =
        new BatteryProvider() {
          @Override
          public int getBatteryLevel() {
            return 37;
          }
        };

    HardwareSnapshot snapshot = provider.readSnapshot();

    assertEquals(37, snapshot.batteryPercent());
  }
}
