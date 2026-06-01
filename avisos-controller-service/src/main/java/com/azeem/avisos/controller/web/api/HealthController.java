/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthReport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {
  private final SystemHealthMonitor systemHealthMonitor;

  public HealthController(SystemHealthMonitor systemHealthMonitor) {
    this.systemHealthMonitor = systemHealthMonitor;
  }

  @GetMapping
  public SystemHealthReport getHealth() {
    systemHealthMonitor.refreshHealth();
    return systemHealthMonitor.checkSystemHealth();
  }
}
