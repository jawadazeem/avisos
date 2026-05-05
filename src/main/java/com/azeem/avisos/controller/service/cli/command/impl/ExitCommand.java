/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.cli.command.api.Command;

public class ExitCommand implements Command {
    private final CliClient cliClient;

    public ExitCommand(CliClient cliClient) {
        this.cliClient = cliClient;
    }

    @Override
    public String description() {
        return """
                Initiates a graceful shutdown of the AVISOS shell. This triggers the cleanup of 
                terminal resources and safely disconnects active background listeners before 
                terminating the process.
                """;
    }

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public void execute(String input) {
        cliClient.shutdown();
    }
}
