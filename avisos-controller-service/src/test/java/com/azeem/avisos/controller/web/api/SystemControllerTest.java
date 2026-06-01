/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SystemControllerTest {

  private SystemController systemController;

  @BeforeEach
  void setUp() {
    systemController = new SystemController();
  }

  @Test
  void getStats_shouldReturnAllExpectedKeys() {
    Map<String, Object> stats = systemController.getStats();

    assertTrue(stats.containsKey("heapUsedMb"));
    assertTrue(stats.containsKey("heapMaxMb"));
    assertTrue(stats.containsKey("availableProcessors"));
    assertTrue(stats.containsKey("activeThreads"));
    assertTrue(stats.containsKey("uptimeSeconds"));
    assertTrue(stats.containsKey("javaVersion"));
  }

  @Test
  void getStats_shouldReturnNonNegativeValues() {
    Map<String, Object> stats = systemController.getStats();

    assertTrue((long) stats.get("heapUsedMb") >= 0);
    assertTrue((long) stats.get("heapMaxMb") > 0);
    assertTrue((int) stats.get("availableProcessors") >= 1);
    assertTrue((int) stats.get("activeThreads") >= 1);
    assertTrue((long) stats.get("uptimeSeconds") >= 0);
  }

  @Test
  void getStats_shouldReturnJavaVersion() {
    Map<String, Object> stats = systemController.getStats();

    String version = (String) stats.get("javaVersion");
    assertNotNull(version);
    assertFalse(version.isBlank());
  }

  @Test
  void getAbout_shouldReturnCorrectSystemInfo() {
    Map<String, String> about = systemController.getAbout();

    assertEquals("AVISOS", about.get("name"));
    assertEquals("Advanced Visual Infrastructure Secure Operational Systems",
        about.get("description"));
    assertEquals("1.0-SNAPSHOT", about.get("version"));
    assertTrue(about.get("architecture").contains("Spring Boot"));
  }

  @Test
  void getAbout_shouldContainAllExpectedKeys() {
    Map<String, String> about = systemController.getAbout();

    assertEquals(4, about.size());
    assertTrue(about.containsKey("name"));
    assertTrue(about.containsKey("description"));
    assertTrue(about.containsKey("version"));
    assertTrue(about.containsKey("architecture"));
  }
}
