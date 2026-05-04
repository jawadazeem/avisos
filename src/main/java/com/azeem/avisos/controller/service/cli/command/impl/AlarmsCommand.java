/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.cli.command.api.Command;

import java.util.List;

/**
 * Displays all active alarms
 */
public class AlarmsCommand implements Command {
    private final AlarmService alarmService;
    private final CliClient cliClient;

    public AlarmsCommand(CliClient cliClient, AlarmService alarmService) {
        this.alarmService = alarmService;
        this.cliClient = cliClient;
    }

    @Override
    public String name() {
        return "alarms";
    }

    @Override
    public void execute(String input) {
        List<AlarmRecord> alarms = alarmService.loadAllActiveAlarms();

        if (alarms.isEmpty()) {
            cliClient.println("No active alarms.");
            return;
        }

        alarms.forEach(alarm ->
                cliClient.println(formatAlarm(alarm))
        );
    }

    private String formatAlarm(AlarmRecord alarm) {
        return "[" + alarm.severity() + "] " + alarm.reason();
    }
}
