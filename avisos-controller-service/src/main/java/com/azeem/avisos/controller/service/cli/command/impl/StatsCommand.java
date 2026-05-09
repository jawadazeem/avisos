/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.service.cli.command.api.Command;

public class StatsCommand implements Command {
    private final CliClient cliClient;

    public StatsCommand(CliClient cliClient) {
        this.cliClient = cliClient;
    }

    @Override
    public String name() { return "stats"; }

    @Override
    public String description() { return "Displays Controller JVM metrics and telemetry cache size."; }

    @Override
    public void execute(String input) {
        Runtime runtime = Runtime.getRuntime();
        long mb = 1024 * 1024;

        cliClient.println("--- CONTROLLER STATS ---");
        cliClient.println("Used Memory:  " + (runtime.totalMemory() - runtime.freeMemory()) / mb + "MB");
        cliClient.println("Max Memory:   " + (runtime.maxMemory() / mb) + "MB");
        cliClient.println("Threads:      " + Thread.activeCount());
    }
}
