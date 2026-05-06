/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.cli.command.api.Command;
import com.azeem.avisos.controller.service.cli.command.api.CommandRegistry;

import java.util.Comparator;

/**
 * Dynamically lists all registered commands and their descriptions.
 */
public class HelpCommand implements Command {
    private final CliClient cliClient;
    private final CommandRegistry registry;

    public HelpCommand(CliClient cliClient, CommandRegistry registry) {
        this.cliClient = cliClient;
        this.registry = registry;
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "Displays this help menu.";
    }

    @Override
    public void execute(String input) {
        cliClient.println("\n--- AVISOS Operational Command List ---");

        registry.getAllCommands().stream()
                .sorted(Comparator.comparing(Command::name))
                .forEach(cmd -> {
                    String formatted = String.format("  %-12s : %s",
                            cmd.name(),
                            cmd.description());
                    cliClient.println(formatted);
                });

        cliClient.println("---------------------------------------\n");
    }
}