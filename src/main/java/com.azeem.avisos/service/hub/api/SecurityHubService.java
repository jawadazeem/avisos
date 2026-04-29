/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.service.hub.api;

import com.azeem.avisos.alarm.Alarm;
import com.azeem.avisos.commands.Command;
import com.azeem.avisos.commands.securityhubcommands.SecurityHubCommand;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.devices.model.DeviceStatus;
import com.azeem.avisos.infrastructure.annotations.Timed;
import com.azeem.avisos.infrastructure.subscribers.Subscriber;

import java.util.List;
import java.util.UUID;

public interface SecurityHubService {

    // Device management
    void addDevice(DataAcquisitionDevice dataAcquisitionDevice);
    void removeDevice(DataAcquisitionDevice dataAcquisitionDevice);
    @Timed
    void activateDevice(DataAcquisitionDevice dataAcquisitionDevice);
    void deactivateDevice(DataAcquisitionDevice dataAcquisitionDevice);
    DeviceStatus getDeviceStatus(DataAcquisitionDevice dataAcquisitionDevice);
    List<DataAcquisitionDevice> getDataAcquisitionDevices();
    void removeAllDevices();
    void resetDataAcquisitionDevices();

    // Hub maintenance
    boolean isMaintenanceMode();
    void toggleIsMaintenanceMode();

    // Commands
    void executeCommand(SecurityHubCommand cmd);
    void addCommand(SecurityHubCommand cmd);
    void processNextCommand();
    void processCommand(Command cmd);
    void processAllCommands();
    void initiateFleetCheck();
    void pingDevices();
    void checkDeviceBatteryLevels();
    void monitorAndHandleDeviceHealth();

    // Alarm handling
    void registerAlarm(Alarm alarm);
    void resolveAlarm(Alarm alarm);
    void resolveAlarmsByDevice(DataAcquisitionDevice dataAcquisitionDevice);
    void resolveAllAlarms();

    // Pairing com.azeem.avisos.devices
    void pairNewDataAcquisitionDeviceById(UUID Id);

    // Subscriber management
    void addReceiver(Subscriber subscriber);
    void removeReceiver(Subscriber subscriber);
}
