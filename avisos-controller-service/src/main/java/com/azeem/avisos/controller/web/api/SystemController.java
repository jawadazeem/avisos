/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for JVM runtime statistics and application metadata. */
@RestController
@RequestMapping("/api/system")
public class SystemController {

  @GetMapping("/stats")
  public Map<String, Object> getStats() {
    Runtime rt = Runtime.getRuntime();
    long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
    Duration uptime = Duration.ofMillis(uptimeMs);

    return Map.of(
        "heapUsedMb", (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024),
        "heapMaxMb", rt.maxMemory() / (1024 * 1024),
        "availableProcessors", rt.availableProcessors(),
        "activeThreads", Thread.activeCount(),
        "uptimeSeconds", uptime.toSeconds(),
        "javaVersion", System.getProperty("java.version"));
  }

  @GetMapping("/about")
  public Map<String, String> getAbout() {
    return Map.of(
        "name", "AVISOS",
        "author", "Jawad Azeem",
        "description", "Advanced Visual Infrastructure Secure Operational Systems",
        "version", "1.0-SNAPSHOT",
        "architecture", "Spring Boot 3.4.1 + MQTT + Protobuf + SQLite");
  }
}
