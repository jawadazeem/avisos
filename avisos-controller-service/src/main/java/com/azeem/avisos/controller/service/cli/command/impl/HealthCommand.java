/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthReport;
import com.azeem.avisos.controller.service.cli.command.api.Command;

public class HealthCommand implements Command {
  private final CliClient cliClient;
  private final SystemHealthMonitor systemHealthMonitor;

  public HealthCommand(CliClient cliClient, SystemHealthMonitor systemHealthMonitor) {
    this.cliClient = cliClient;
    this.systemHealthMonitor = systemHealthMonitor;
  }

  @Override
  public String name() {
    return "health";
  }

  @Override
  public String description() {
    return "Displays the application's latest health diagnostic summary.";
  }

  @Override
  public void execute(String ignored) {
    systemHealthMonitor.refreshHealth();
    SystemHealthReport report = systemHealthMonitor.checkSystemHealth();
    cliClient.println("Overall Status: " + report.overallStatus().toString());
  }
}
