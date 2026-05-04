/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.cli.command.api.CommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JLineCliService implements CliService {
    private static final Logger log = LoggerFactory.getLogger(JLineCliService.class);
    private final CliClient cliClient;
    private final CommandRegistry commandRegistry;
    private final ExecutorService executor;

    public JLineCliService(CliClient cliClient,
                           CommandRegistry commandRegistry,
                           ExecutorService executor
    ) {
        this.cliClient = cliClient;
        this.commandRegistry = commandRegistry;
        this.executor = executor;
    }

    @Override
    public void runCommand() {
        cliClient.init();
        while (cliClient.isRunning()) {
            try {
                String input = cliClient.readLn().trim();
                commandRegistry.find(input)
                        .ifPresentOrElse(
                                cmd -> executor.submit(() -> cmd.execute(input)),
                                () -> cliClient.println("Unknown command: " + input)
                        );
            } catch (IllegalStateException e) {
                break; // terminal closed externally;
            }
        }
    }
}
