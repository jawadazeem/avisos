/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.node.NodeService;
import com.azeem.avisos.controller.service.cli.command.api.Command;
import java.util.UUID;

public class InspectCommand implements Command {
    private final NodeService nodeService;
    private final CliClient cliClient;

    public InspectCommand(CliClient cliClient, NodeService nodeService) {
        this.nodeService = nodeService;
        this.cliClient = cliClient;
    }

    @Override
    public String name() { return "inspect"; }

    @Override
    public String description() { return "Provides detailed metadata for a specific node. Usage: inspect <uuid>"; }

    @Override
    public void execute(String input) {
        String[] parts = input.split("\\s+");
        if (parts.length < 2) {
            cliClient.println("Error: Missing UUID. Usage: inspect <uuid>");
            return;
        }

        try {
            UUID id = UUID.fromString(parts[1]);
            nodeService.getNode(id).ifPresentOrElse(
                    node -> {
                        cliClient.println("--- NODE METADATA ---");
                        cliClient.println("ID:     " + node.uuid());
                        cliClient.println("Name:   " + node.name());
                        cliClient.println("Type:   " + node.type());
                        cliClient.println("Status: " + node.status());
                        cliClient.println("Power:  " + node.batteryLevel() + "%");
                        cliClient.println("Last:   " + node.lastSeen());
                    },
                    () -> cliClient.println("Node not found in registry.")
            );
        } catch (IllegalArgumentException e) {
            cliClient.println("Invalid UUID format.");
        }
    }
}
