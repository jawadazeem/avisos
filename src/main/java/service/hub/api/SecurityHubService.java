/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package service.hub.api;

import alarm.Alarm;
import commands.Command;
import commands.securityhubcommands.SecurityHubCommand;
import devices.api.DataAcquisitionDevice;
import devices.model.DeviceStatus;
import infrastructure.annotations.Timed;
import infrastructure.subscribers.Subscriber;

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

    // Pairing devices
    void pairNewDataAcquisitionDeviceById(UUID Id);

    // Subscriber management
    void addReceiver(Subscriber subscriber);
    void removeReceiver(Subscriber subscriber);
}
