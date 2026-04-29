/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.commands.alarmcommands;

import com.azeem.avisos.alarm.Alarm;
import com.azeem.avisos.alarm.AlarmSeverity;
import com.azeem.avisos.alarm.AlarmStatus;
import com.azeem.avisos.commands.CommandType;
import com.azeem.avisos.core.SecurityHub;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;

import java.time.LocalDateTime;

public class TriggerAlarmCommand implements AlarmCommand {
    private DataAcquisitionDevice dataAcquisitionDevice;
    private String message;
    private AlarmSeverity alarmSeverity;
    private CommandType commandType = CommandType.ALARM;

    public TriggerAlarmCommand(DataAcquisitionDevice dataAcquisitionDevice, String message, AlarmSeverity alarmSeverity) {
        this.dataAcquisitionDevice = dataAcquisitionDevice;
        this.message = message;
        this.alarmSeverity = alarmSeverity;
    }

    @Override
    public void execute() {
        SecurityHub.getInstance().registerAlarm(new Alarm(dataAcquisitionDevice.getDeviceType(), dataAcquisitionDevice.getId(), AlarmStatus.ACTIVE, alarmSeverity, LocalDateTime.now()));
        dataAcquisitionDevice.updateAllSubscribers(message);
    }

    @Override
    public AlarmSeverity getSeverity() {
        return alarmSeverity;
    }

    @Override
    public DataAcquisitionDevice getDevice() {
        return dataAcquisitionDevice;
    }

    @Override
    public CommandType getCommandType() {
        return commandType;
    }

    @Override
    public boolean validate() {
        return !message.isBlank();
    }

    @Override
    public String validationMessage() {
        if (message.isBlank()) {
            return "Command validation error for Trigger Alarm Command, the message you have provided is blank.";
        }
        return "Trigger Alarm Command was validated successfully";
    }
}
