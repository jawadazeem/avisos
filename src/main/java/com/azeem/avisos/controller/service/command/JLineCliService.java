/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.command;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;

import java.util.List;

public class JLineCliService implements CliService {
    private final CliClient cliClient;
    private final List<String> exitKeywords;

    public JLineCliService(CliClient cliClient) {
        this.cliClient = cliClient;
        this.exitKeywords = List.of("exit", "quit", "q");
    }

    @Override
    public void run() {
        while (true) {
            cliClient.println("Enter a command (type 'exit' to quit):");
            String input = cliClient.readLn();
            if (isExitCommand(input)) {
                cliClient.shutdown();
                break;
            }

            handleCommand(cliClient.readLn());
        }
    }

    private boolean isExitCommand(String input) {
        return exitKeywords.contains(input.trim().toLowerCase());
    }

    private void handleCommand(String command) {
        cliClient.println("You have entered: " + command);
    }

    // TODO: Implement a bunch of private methods that act as an orchestrator across the system
    //  calling various methods from all services across the application.
}
