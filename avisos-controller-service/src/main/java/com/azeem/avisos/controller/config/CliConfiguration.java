/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.infrastructure.cli.JLineCliClient;
import com.azeem.avisos.controller.infrastructure.cli.command.CommandRegistry;
import com.azeem.avisos.controller.infrastructure.cli.command.InMemoryCommandRegistry;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor;
import com.azeem.avisos.controller.security.model.SecurityContext;
import com.azeem.avisos.controller.security.service.AuthService;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.cli.CliService;
import com.azeem.avisos.controller.service.cli.JLineCliService;
import com.azeem.avisos.controller.service.cli.command.impl.*;
import com.azeem.avisos.controller.service.node.NodeService;
import java.util.concurrent.Executors;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * CLI configuration -- only active when {@code avisos.cli.enabled=true}. In web/Docker mode the CLI
 * is disabled by default so it does not block Spring Boot startup.
 */
@Configuration
@ConditionalOnProperty(name = "avisos.cli.enabled", havingValue = "true")
public class CliConfiguration {

  @Bean
  public CliClient cliClient() {
    return new JLineCliClient();
  }

  @Bean
  public CommandRegistry commandRegistry(
      CliClient cliClient,
      AlarmService alarmService,
      NodeService nodeService,
      SystemHealthMonitor monitor,
      NodeServiceConfig nodeServiceConfig) {
    CommandRegistry registry = new InMemoryCommandRegistry();
    registry.register(new ExitCommand(cliClient));
    registry.register(new AlarmsCommand(cliClient, alarmService));
    registry.register(new HelpCommand(cliClient, registry));
    registry.register(new InspectCommand(cliClient, nodeService));
    registry.register(new NodesCommand(cliClient, nodeService));
    registry.register(new StatsCommand(cliClient));
    registry.register(new AboutCommand(cliClient));
    registry.register(new HealthCommand(cliClient, monitor));
    registry.register(new PurgeCommand(cliClient, nodeService, nodeServiceConfig));
    return registry;
  }

  @Bean
  public CliService cliService(
      CliClient cliClient,
      CommandRegistry commandRegistry,
      AuthService authService,
      SecurityContext securityContext) {
    return new JLineCliService(
        cliClient,
        commandRegistry,
        authService,
        securityContext,
        Executors.newVirtualThreadPerTaskExecutor());
  }

  /** Launches the JLine REPL on a non-daemon thread alongside the Spring web server. */
  @Bean
  public CommandLineRunner cliRunner(CliService cliService) {
    return args -> {
      Thread cliThread = Thread.ofVirtual().name("CLI-Thread").unstarted(cliService::start);
      cliThread.setDaemon(false);
      cliThread.start();
    };
  }
}
