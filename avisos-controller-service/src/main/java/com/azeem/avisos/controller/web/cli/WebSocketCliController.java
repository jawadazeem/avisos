/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.cli;

import com.azeem.avisos.controller.config.NodeServiceConfig;
import com.azeem.avisos.controller.infrastructure.cli.BufferingCliClient;
import com.azeem.avisos.controller.infrastructure.cli.command.CommandRegistry;
import com.azeem.avisos.controller.infrastructure.cli.command.InMemoryCommandRegistry;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.cli.command.api.Command;
import com.azeem.avisos.controller.service.cli.command.impl.AboutCommand;
import com.azeem.avisos.controller.service.cli.command.impl.AlarmsCommand;
import com.azeem.avisos.controller.service.cli.command.impl.HealthCommand;
import com.azeem.avisos.controller.service.cli.command.impl.HelpCommand;
import com.azeem.avisos.controller.service.cli.command.impl.InspectCommand;
import com.azeem.avisos.controller.service.cli.command.impl.NodesCommand;
import com.azeem.avisos.controller.service.cli.command.impl.PurgeCommand;
import com.azeem.avisos.controller.service.cli.command.impl.StatsCommand;
import com.azeem.avisos.controller.service.node.NodeService;
import java.util.Optional;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * Bridges the CLI command framework to the web dashboard over STOMP WebSocket. Creates a fresh
 * {@link BufferingCliClient} and command registry per request to capture output without shared
 * state.
 */
@Controller
public class WebSocketCliController {
  private final AlarmService alarmService;
  private final NodeService nodeService;
  private final SystemHealthMonitor healthMonitor;
  private final NodeServiceConfig nodeServiceConfig;

  public WebSocketCliController(
      AlarmService alarmService,
      NodeService nodeService,
      SystemHealthMonitor healthMonitor,
      NodeServiceConfig nodeServiceConfig) {
    this.alarmService = alarmService;
    this.nodeService = nodeService;
    this.healthMonitor = healthMonitor;
    this.nodeServiceConfig = nodeServiceConfig;
  }

  @MessageMapping("/cli")
  @SendTo("/topic/cli")
  public CliResponseMessage handleCommand(CliCommandMessage message) {
    long start = System.currentTimeMillis();

    BufferingCliClient client = new BufferingCliClient();
    InMemoryCommandRegistry registry = new InMemoryCommandRegistry();

    registry.register(new AboutCommand(client));
    registry.register(new AlarmsCommand(client, alarmService));
    registry.register(new HealthCommand(client, healthMonitor));
    registry.register(new HelpCommand(client, registry));
    registry.register(new InspectCommand(client, nodeService));
    registry.register(new NodesCommand(client, nodeService));
    registry.register(new PurgeCommand(client, nodeService, nodeServiceConfig));
    registry.register(new StatsCommand(client));

    String input = message.command();
    Optional<Command> command = registry.find(input);

    if (command.isPresent()) {
      command.get().execute(input);
    } else {
      client.println("Unknown command: " + input.split("\\s+")[0]);
      client.println("Type 'help' for available commands.");
    }

    long elapsed = System.currentTimeMillis() - start;
    return new CliResponseMessage(input, client.getOutput(), elapsed);
  }
}
