/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.config.NodeServiceConfig;
import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.cli.command.api.Command;
import com.azeem.avisos.controller.service.node.NodeService;

public class PurgeCommand implements Command {
  private final NodeService nodeService;
  private final CliClient cliClient;
  private final NodeServiceConfig serviceConfig;

  public PurgeCommand(
      CliClient cliClient, NodeService nodeService, NodeServiceConfig serviceConfig) {
    this.nodeService = nodeService;
    this.cliClient = cliClient;
    this.serviceConfig = serviceConfig;
  }

  @Override
  public String name() {
    return "purge";
  }

  @Override
  public String description() {
    return "Manually triggers the eviction of unresponsive nodes (>"
        + serviceConfig.staleThreshold()
        + "s)";
  }

  @Override
  public void execute(String input) {
    cliClient.println("Scanning registry for stale nodes...");
    nodeService.checkStaleNodes();
    cliClient.println("Purge cycle complete.");
  }
}
