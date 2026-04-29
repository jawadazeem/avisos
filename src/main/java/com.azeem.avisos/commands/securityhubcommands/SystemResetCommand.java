/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.commands.securityhubcommands;

import com.azeem.avisos.alarm.AlarmSeverity;
import com.azeem.avisos.commands.CommandType;
import com.azeem.avisos.core.SecurityHub;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.devices.model.DeviceStatus;

public class SystemResetCommand implements SecurityHubCommand {

    private DataAcquisitionDevice dataAcquisitionDevice;
    private CommandType commandType = CommandType.RESET;

    public SystemResetCommand(DataAcquisitionDevice dataAcquisitionDevice) {
        this.dataAcquisitionDevice = dataAcquisitionDevice;
    }

    @Override
    public void execute() {
        dataAcquisitionDevice.setDeviceStatus(DeviceStatus.OPERATIONAL);
        dataAcquisitionDevice.resetFailureCount();
        SecurityHub.getInstance().resolveAlarmsByDevice(dataAcquisitionDevice);
    }

    @Override
    public AlarmSeverity getSeverity() {
        return AlarmSeverity.MEDIUM;
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
        return true;
    }

    @Override
    public String validationMessage() {
        return "System Reset Command was validated successfully";
    }
}
