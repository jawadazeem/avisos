/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.commands;

import com.azeem.avisos.alarm.AlarmSeverity;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;

public interface Command {
    void execute();
    boolean validate();
    String validationMessage();
    AlarmSeverity getSeverity();
    CommandType getCommandType();
    DataAcquisitionDevice getDevice();
}
