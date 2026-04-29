/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package commands.alarmcommands;

import alarm.Alarm;
import alarm.AlarmSeverity;
import alarm.AlarmStatus;
import commands.CommandType;
import core.SecurityHub;
import devices.api.DataAcquisitionDevice;

import java.time.LocalDateTime;

public class PanicAlarmCommand implements AlarmCommand {
    private DataAcquisitionDevice dataAcquisitionDevice;
    private String message;
    private CommandType commandType = CommandType.HIGH_PRIORITY_ALARM;

    public PanicAlarmCommand(DataAcquisitionDevice dataAcquisitionDevice, String message) {
        this.dataAcquisitionDevice = dataAcquisitionDevice;
        this.message = message;
    }

    @Override
    public void execute() {
        SecurityHub.getInstance().registerAlarm(new Alarm(dataAcquisitionDevice.getDeviceType(), dataAcquisitionDevice.getId(), AlarmStatus.ACTIVE, AlarmSeverity.CRITICAL, LocalDateTime.now()));
        dataAcquisitionDevice.updateAllSubscribers(message);
    }

    @Override
    public AlarmSeverity getSeverity() {
        return AlarmSeverity.CRITICAL;
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
        return !validationMessage().contains("error");
    }

    @Override
    public String validationMessage() {
        if (dataAcquisitionDevice.getBatteryLife() < 5 && (message == null || message.isBlank())) {
            return "Command validation error, the message you have provided is blank and the dataAcquisitionDevice's battery life is less than 5%.";
        }
        if (dataAcquisitionDevice.getBatteryLife() < 5) {
            return "Command validation error, the dataAcquisitionDevice's battery life is less than 5%.";
        }
        if (message.isBlank()) {
            return "Command validation error for Panic Command, the message you have provided is blank.";
        }
        return "Panic Command was validated successfully";
    }
}
