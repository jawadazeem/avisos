/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.commands.securityhubcommands;

import com.azeem.avisos.alarm.AlarmSeverity;
import com.azeem.avisos.commands.CommandType;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;

public class SystemDiagnosticCommand implements SecurityHubCommand {
    private DataAcquisitionDevice dataAcquisitionDevice;
    private CommandType commandType = CommandType.DIAGNOSTIC;

    public SystemDiagnosticCommand(DataAcquisitionDevice dataAcquisitionDevice) {
        this.dataAcquisitionDevice = dataAcquisitionDevice;
    }

    @Override
    public void execute() {
        dataAcquisitionDevice.performSelfCheck();
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
        return "System Diagnostic Command was validated successfully";
    }
}
