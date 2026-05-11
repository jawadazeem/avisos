/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.cli.command.impl.ExitCommand;
import org.junit.jupiter.api.Test;

public class ExitCommandTest {
  @Test
  void shouldShutdownCliClientWhenExitCommandRuns() {
    CliClient cliClient = mock(CliClient.class);

    ExitCommand command = new ExitCommand(cliClient);

    command.execute("");

    verify(cliClient).shutdown();
  }
}
