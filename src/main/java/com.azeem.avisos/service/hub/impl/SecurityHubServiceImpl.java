/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.service.hub.impl;

import com.azeem.avisos.alarm.Alarm;
import com.azeem.avisos.alarm.AlarmSeverity;
import com.azeem.avisos.commands.Command;
import com.azeem.avisos.commands.CommandType;
import com.azeem.avisos.commands.securityhubcommands.SecurityHubCommand;
import com.azeem.avisos.commands.securityhubcommands.SystemDiagnosticCommand;
import com.azeem.avisos.commands.securityhubcommands.SystemResetCommand;
import com.azeem.avisos.core.HubStatus;
import com.azeem.avisos.core.SecurityHub;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.devices.impl.RFDataAcquisitionDeviceDetectorDevice;
import com.azeem.avisos.devices.model.DeviceStatus;
import com.azeem.avisos.infrastructure.logger.LogLevel;
import com.azeem.avisos.infrastructure.logger.Logger;
import com.azeem.avisos.infrastructure.repository.AlarmLogRepository;
import com.azeem.avisos.infrastructure.repository.DeviceRepository;
import com.azeem.avisos.infrastructure.subscribers.Subscriber;
import com.azeem.avisos.service.hub.api.SecurityHubService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SecurityHubServiceImpl implements SecurityHubService {
    private final SecurityHub hub;
    private final Logger logger;
    private final RFDataAcquisitionDeviceDetectorDevice rfDataAcquisitionDeviceDetectorDevice;
    private final AlarmLogRepository alarmRepo;
    private final DeviceRepository deviceRepo;

    public SecurityHubServiceImpl(SecurityHub hub,
                                  Logger logger,
                                  RFDataAcquisitionDeviceDetectorDevice
                              rfDataAcquisitionDeviceDetectorDevice,
                                  AlarmLogRepository alarmRepo,
                                  DeviceRepository deviceRepo) {
        this.hub = hub;
        this.logger = logger;
        this.rfDataAcquisitionDeviceDetectorDevice = rfDataAcquisitionDeviceDetectorDevice;
        this.alarmRepo = alarmRepo;
        this.deviceRepo = deviceRepo;
    }

    @Override
    public void addDevice(DataAcquisitionDevice dataAcquisitionDevice) {
        deviceRepo.loadAll().add(dataAcquisitionDevice);
        logger.log( "A new " + dataAcquisitionDevice.getDeviceType() + " dataAcquisitionDevice (ID: " + dataAcquisitionDevice.getId() + ") was added to the dataAcquisitionDevices list.", LogLevel.INFO);

        if (deviceRepo != null) {
            deviceRepo.save(dataAcquisitionDevice);
        } else {
            logger.log("Warning: DataAcquisitionDevice added but Repository not initialized.", LogLevel.WARNING);
        }
    }

    @Override
    public void removeDevice(DataAcquisitionDevice dataAcquisitionDevice) {
        hub.removeDevice(dataAcquisitionDevice);

        deviceRepo.remove(dataAcquisitionDevice);

        logger.log("Device removed successfully from memory and storage.", LogLevel.INFO);
    }

    @Override
    public void activateDevice(DataAcquisitionDevice dataAcquisitionDevice) {
        dataAcquisitionDevice.setDeviceStatus(DeviceStatus.OPERATIONAL);
        logger.log(dataAcquisitionDevice.getId() + " was activated.", LogLevel.INFO);
    }

    @Override
    public void deactivateDevice(DataAcquisitionDevice dataAcquisitionDevice) {
        dataAcquisitionDevice.setDeviceStatus(DeviceStatus.AWAY);
        logger.log(dataAcquisitionDevice.getId() + " was deactivated.", LogLevel.INFO);
    }

    @Override
    public DeviceStatus getDeviceStatus(DataAcquisitionDevice dataAcquisitionDevice) {
        return dataAcquisitionDevice.getDeviceStatus();
    }

    @Override
    public List<DataAcquisitionDevice> getDataAcquisitionDevices() {
        return deviceRepo.loadAll();
    }

    @Override
    public boolean isMaintenanceMode() {
        return hub.isMaintenanceMode();
    }

    @Override
    public void toggleIsMaintenanceMode() {
        hub.toggleIsMaintenanceMode();
    }

    /** Use this instead of individually calling addCommand and processCommand,
     * both of which are for primarily for debugging.
     */
    @Override
    public void executeCommand(SecurityHubCommand cmd) {
        addCommand(cmd);
        processCommand(cmd);
    }

    //TODO: Implement Multithreading
    @Override
    public void addCommand(SecurityHubCommand cmd) {
        if (!cmd.validate()) {
            logger.log(cmd.validationMessage(), LogLevel.ERROR);
            return;
        }

        if (cmd.getCommandType() == CommandType.RESET || cmd.getCommandType() == CommandType.DIAGNOSTIC) {
            hub.getTaskQueue().add(cmd);
            return;
        }

        if (cmd.getDevice().getDeviceStatus() != DeviceStatus.OPERATIONAL) {
            logger.log("Command unsuccessful, must target an operational device.", LogLevel.ERROR);
            return;
        }

        if (hub.getStatus() == HubStatus.DISARMED) {
            logger.log("The hub must be armed to trigger it. Current Status: DISARMED", LogLevel.WARNING);

        } else {
            if (cmd.getSeverity() != AlarmSeverity.LOW && !hub.isMaintenanceMode()) {
                hub.getTaskQueue().add(cmd);
                logger.log("Successfully added security hub com.azeem.avisos.commands.", LogLevel.INFO);
            }
        }
    }

    //TODO: Implement Multithreading
    @Override
    public void processNextCommand() {
        if (!hub.getTaskQueue().isEmpty()) {
            hub.getTaskQueue().poll().execute();
        }
        if (hub.getTaskQueue().isEmpty()) {
            logger.log("The com.azeem.avisos.commands queue is now empty. All com.azeem.avisos.commands processed.", LogLevel.INFO);
        }
    }

    //TODO: Implement Multithreading
    @Override
    public void processCommand(Command cmd) {
        if (hub.getStatus() != HubStatus.ARMED) {
            logger.log("Hub must be armed to process com.azeem.avisos.commands", LogLevel.WARNING);
            return;
        }
        cmd.execute();
    }

    //TODO: Implement Multithreading
    @Override
    public void processAllCommands() {
        if (hub.getStatus() != HubStatus.ARMED) {
            logger.log("Hub must be armed to process com.azeem.avisos.commands", LogLevel.WARNING);
            return;
        }

        while (!hub.getTaskQueue().isEmpty()) {
            processNextCommand();
        }
    }

    @Override
    public void registerAlarm(Alarm alarm) {
        hub.getActiveDevicesAndAssociatedAlarms().put(alarm.getDeviceId(), alarm);
        if (alarmRepo != null) {
            alarmRepo.save(alarm);
        } else {
            logger.log("Warning: Alarm triggered but Repository not initialized.", LogLevel.WARNING);
        }

        logger.log("Alarm registered: " + alarm.getDeviceId(), LogLevel.INFO);
    }

    @Override
    public void pairNewDataAcquisitionDeviceById(UUID Id) {
        if (hub.getRfDataAcquisitionDeviceDetectorDevice() != null) {
            hub.getRfDataAcquisitionDeviceDetectorDevice().pair(Id);
        } else {
            logger.log("Radio Frequency Data Acquisition DeviceDetectorDevice is non-existent", LogLevel.WARNING);
        }
    }

    private void updateDeviceStatus() {
        List<DataAcquisitionDevice> devicesToDecommission = new ArrayList<>();

        for (DataAcquisitionDevice d : getDataAcquisitionDevices()) {
            int numFails = d.getFailureCount();

            if (numFails == 1 || numFails == 2) {
                logger.log("DataAcquisitionDevice failure count is " + numFails, LogLevel.WARNING);
            } else if (numFails == 3) {
                d.setDeviceStatus(DeviceStatus.RECOVERY_MODE);
                logger.log("DataAcquisitionDevice failure count is " + numFails + ". The device is in " + d.getDeviceStatus(), LogLevel.CRITICAL);
                hub.getTaskQueue().addFirst(new SystemDiagnosticCommand(d));
            } else if (numFails == 4) {
                logger.log("DataAcquisitionDevice failure count is " + numFails + ". The device is in " + d.getDeviceStatus(), LogLevel.CRITICAL);
                hub.getTaskQueue().addFirst(new SystemResetCommand(d));
            } else if (numFails == 5) {
                d.setDeviceStatus(DeviceStatus.DECOMMISSIONED);
                devicesToDecommission.add(d);
                hub.getDataAcquisitionDevices().add(d);
                logger.log("DataAcquisitionDevice failure count is " + numFails + ". The device is in " + d.getDeviceStatus(), LogLevel.CRITICAL);
            }
        }
        getDataAcquisitionDevices().removeIf(devicesToDecommission::contains);
    }

    //TODO: Implement Multithreading
    @Override
    public void resolveAlarm(Alarm alarm) {
        if (hub.getStatus() != HubStatus.ARMED) {
            logger.log("Hub must be armed to resolve active alarms", LogLevel.WARNING);
            return;
        }
        alarm.resolveAlarm();
        hub.getActiveAlarms().remove(alarm.getDeviceId());
    }

    //TODO: Implement Multithreading
    public void resolveAlarmsByDevice(DataAcquisitionDevice dataAcquisitionDevice) {
        if (hub.getStatus() != HubStatus.ARMED) {
            logger.log("Hub must be armed to resolve active alarms", LogLevel.WARNING);
            return;
        }
        hub.getActiveAlarms().remove(dataAcquisitionDevice.getId());
    }

    //TODO: Implement Multithreading
    @Override
    public void resolveAllAlarms() {
        if (hub.getStatus() != HubStatus.ARMED) {
            logger.log("Hub must be armed to resolve active alarms", LogLevel.WARNING);
            return;
        }
        hub.getActiveAlarms().clear();
    }

    @Override
    public void removeAllDevices() {
        getDataAcquisitionDevices().clear();

        if (deviceRepo != null) {
            deviceRepo.removeAll();
        }
    }

    @Override
    public void initiateFleetCheck() {
        logger.log("Initiated fleet check. Any errors will show below.", LogLevel.HEALTH);
        for (DataAcquisitionDevice d : getDataAcquisitionDevices()) {
            addCommand(new SystemDiagnosticCommand(d));
        }
    }

    @Override
    public void pingDevices() {
        for (DataAcquisitionDevice d : getDataAcquisitionDevices()) {
            d.ping();
        }
    }

    @Override
    public void checkDeviceBatteryLevels() {
        int numLowBatteriedDevices = 0;

        for (DataAcquisitionDevice d : getDataAcquisitionDevices()) {
            if (d.getBatteryLife() < 10) {
                logger.log(d.getId() + " is low on charge", LogLevel.HEALTH);
                numLowBatteriedDevices++;
            }
        }
        logger.log("There are " + numLowBatteriedDevices + " low batteried dataAcquisitionDevices.", LogLevel.HEALTH);
    }

    @Override
    public void monitorAndHandleDeviceHealth() {
        updateDeviceFailureState();
        updateDeviceStatus();
    }

    private void updateDeviceFailureState() {
        for (DataAcquisitionDevice d : getDataAcquisitionDevices()) {
            if (!d.ping()) {
                d.incrementFailureCount();
                logger.log("DataAcquisitionDevice ping failed", LogLevel.WARNING);
            } else {
                d.resetFailureCount();
            }
        }
    }

    @Override
    public void addReceiver(Subscriber subscriber) {
        hub.getSubscribers().add(subscriber);
    }

    @Override
    public void removeReceiver(Subscriber subscriber) {
        hub.getSubscribers().remove(subscriber);
    }

    private void performSelfCheck(DataAcquisitionDevice device) {
        device.performSelfCheck();
    }

    @Override
    public void resetDataAcquisitionDevices() {
        for (DataAcquisitionDevice d : hub.getDevices()) {
            executeCommand(new SystemResetCommand(d));
        }
    }
}
