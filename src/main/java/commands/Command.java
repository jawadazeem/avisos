/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package commands;

import alarm.AlarmSeverity;
import devices.api.DataAcquisitionDevice;

public interface Command {
    void execute();
    boolean validate();
    String validationMessage();
    AlarmSeverity getSeverity();
    CommandType getCommandType();
    DataAcquisitionDevice getDevice();
}
