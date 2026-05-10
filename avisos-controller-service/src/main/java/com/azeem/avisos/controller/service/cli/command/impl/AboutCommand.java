/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.cli.command.api.Command;

public class AboutCommand implements Command {
    private final CliClient cliClient;

    public AboutCommand(CliClient cliClient) {
        this.cliClient = cliClient;
    }

    @Override
    public String name() {
        return "about";
    }

    @Override
    public String description() {
        return "Displays the about menu";
    }

    @Override
    public void execute(String input) {
        cliClient.println("""
            ----------------------------------------------------------------------
            AVISOS | Distributed SCADA Orchestrator
            ----------------------------------------------------------------------
            A mission-critical control interface for real-time monitoring and 
            autonomous threat detection within camera networks.
            
            Architected and Developed by: Jawad Azeem
            Build Version: 2026.1.0-STABLE
            
            License: Apache License 2.0
            (C) Copyright 2026 Jawad Azeem. All Rights Reserved.
            ----------------------------------------------------------------------
            Target Environment: Infrastructure Security
            System Status: Operational
            ----------------------------------------------------------------------
            """);
    }
}
