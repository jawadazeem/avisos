/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.node.NodeService;
import com.azeem.avisos.controller.service.cli.command.api.Command;

public class NodesCommand implements Command {
    private final NodeService nodeService;
    private final CliClient cliClient;

    public NodesCommand(CliClient cliClient, NodeService nodeService) {
        this.nodeService = nodeService;
        this.cliClient = cliClient;
    }

    @Override
    public String name() { return "nodes"; }

    @Override
    public String description() {
        return "Lists all registered node UUIDs and their current last-seen delta.";
    }

    @Override
    public void execute(String input) {
        var nodes = nodeService.getRegisteredNodes();
        if (nodes.isEmpty()) {
            cliClient.println("Registry is empty. No nodes discovered.");
            return;
        }

        cliClient.println(String.format("%-40s | %s", "NODE UUID", "STATUS"));
        cliClient.println("------------------------------------------------------------");
        nodes.forEach(id -> cliClient.println(String.format("%-40s | REGISTERED", id)));
    }
}