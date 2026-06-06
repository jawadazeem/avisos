/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.cli;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.azeem.avisos.controller.config.NodeServiceConfig;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.node.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WebSocketCliControllerTest {

  @Mock AlarmService alarmService;
  @Mock NodeService nodeService;
  @Mock SystemHealthMonitor healthMonitor;

  private WebSocketCliController controller;

  @BeforeEach
  void setUp() {
    NodeServiceConfig config = new NodeServiceConfig(300, 5000);
    controller = new WebSocketCliController(alarmService, nodeService, healthMonitor, config);
  }

  @Test
  void handleCommand_shouldReturnOutputForKnownCommand() {
    CliResponseMessage response = controller.handleCommand(new CliCommandMessage("about"));

    assertNotNull(response);
    assertEquals("about", response.command());
    assertFalse(response.output().isBlank());
    assertTrue(response.executionMs() >= 0);
  }

  @Test
  void handleCommand_shouldReturnOutputForStatsCommand() {
    CliResponseMessage response = controller.handleCommand(new CliCommandMessage("stats"));

    assertEquals("stats", response.command());
    assertFalse(response.output().isBlank());
  }

  @Test
  void handleCommand_shouldReturnHelpOutput() {
    CliResponseMessage response = controller.handleCommand(new CliCommandMessage("help"));

    assertEquals("help", response.command());
    assertFalse(response.output().isBlank());
  }

  @Test
  void handleCommand_shouldReturnErrorForUnknownCommand() {
    CliResponseMessage response = controller.handleCommand(new CliCommandMessage("foobar"));

    assertEquals("foobar", response.command());
    assertTrue(response.output().contains("Unknown command"));
    assertTrue(response.output().contains("foobar"));
  }

  @Test
  void handleCommand_shouldHandleEmptyCommandGracefully() {
    CliResponseMessage response = controller.handleCommand(new CliCommandMessage(""));

    assertEquals("", response.command());
    assertTrue(response.output().contains("Unknown command"));
  }

  @Test
  void handleCommand_shouldTrackExecutionTime() {
    CliResponseMessage response = controller.handleCommand(new CliCommandMessage("about"));

    assertTrue(response.executionMs() >= 0);
  }

  @Test
  void handleCommand_shouldPreserveOriginalInput() {
    String input = "inspect node-01";
    CliResponseMessage response = controller.handleCommand(new CliCommandMessage(input));

    assertEquals(input, response.command());
  }
}
