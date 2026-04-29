/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package core;

import alarm.Alarm;
import alarm.AlarmSeverity;
import commands.*;
import commands.securityhubcommands.*;
import devices.api.DataAcquisitionDevice;
import devices.impl.RFDataAcquisitionDeviceDetectorDevice;
import devices.model.DeviceStatus;
import frontend.ReceiverPanel;
import infrastructure.annotations.ServiceAudit;
import infrastructure.annotations.Timed;
import infrastructure.logger.*;
import infrastructure.repository.AlarmLogRepository;
import infrastructure.repository.DeviceRepository;
import infrastructure.subscribers.Subscriber;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

// TODO: Delete deprecated methods, shift usages to use SecurityHubServiceImpl

public class SecurityHub {
    private static SecurityHub hub;
    private final Logger logger;
    private final List<DataAcquisitionDevice> dataAcquisitionDevices = new CopyOnWriteArrayList<>();
    private final List<DataAcquisitionDevice> decommissionedDataAcquisitionDevices = new ArrayList<>();
    private final List<Subscriber> subscribers = new ArrayList<>();
    private final Deque<SecurityHubCommand> taskQueue = new ArrayDeque<>();
    private final Map<UUID, Alarm> activeAlarms = new ConcurrentHashMap<>();
    private RFDataAcquisitionDeviceDetectorDevice rfDataAcquisitionDeviceDetectorDevice;
    private HubStatus status = HubStatus.DISARMED;
    private boolean isMaintenanceMode = false;
    private AlarmLogRepository alarmRepo;
    private DeviceRepository deviceRepo;

    private SecurityHub() {
        this.logger = new TimestampLogger(new ConsoleLogger());
    }

    @Deprecated
    @ServiceAudit("Critical: Saving a new device to the system")
    public void addDevice(DataAcquisitionDevice dataAcquisitionDevice) {
        dataAcquisitionDevices.add(dataAcquisitionDevice);
        logger.log( "A new " + dataAcquisitionDevice.getDeviceType() + " Data Acquisition Device (ID: " + dataAcquisitionDevice.getId() + ") was added to the dataAcquisitionDevices list.", LogLevel.INFO);

        if (deviceRepo != null) {
            deviceRepo.save(dataAcquisitionDevice);
        } else {
            logger.log("Warning: DataAcquisitionDevice added but Repository not initialized.", LogLevel.WARNING);
        }
    }

    @Deprecated
    public void removeDevice(DataAcquisitionDevice dataAcquisitionDevice) {
        if (dataAcquisitionDevices.contains(dataAcquisitionDevice) && deviceRepo != null) {
            dataAcquisitionDevices.remove(dataAcquisitionDevice);
            deviceRepo.remove(dataAcquisitionDevice);
        } else {
            logger.log("An attempt was made to remove a nonexistent Data Acquisition Device.", LogLevel.WARNING);
        }
    }

    @Deprecated
    public void setAlarmRepository(AlarmLogRepository repo) {
        this.alarmRepo = repo;
    }

    @Deprecated
    public void setRfDataAcquisitionDeviceDetectorDevice(RFDataAcquisitionDeviceDetectorDevice rfDataAcquisitionDeviceDetectorDevice) {
        this.rfDataAcquisitionDeviceDetectorDevice = rfDataAcquisitionDeviceDetectorDevice;
    }

    public AlarmLogRepository getAlarmRepo() {
        return alarmRepo;
    }

    public DeviceRepository getDeviceRepo() {
        return deviceRepo;
    }

    @Deprecated
    public void setDeviceRepo(DeviceRepository repo) {
        this.deviceRepo = repo;
    }

    public Logger getLogger() {
        return logger;
    }

    public HubStatus currentMode() {
        return status;
    }

    @Deprecated
    public List<DataAcquisitionDevice> getDataAcquisitionDevices() {
        return dataAcquisitionDevices;
    }

    @Deprecated
    public void processNextCommand() {
        if (!taskQueue.isEmpty()) {
            taskQueue.poll().execute();
        }
        if (taskQueue.isEmpty()) {
            logger.log("The commands queue is now empty. All commands processed.", LogLevel.INFO);
        }
    }

    public Deque<SecurityHubCommand> getTaskQueue() {
        return taskQueue;
    }

    public HubStatus getStatus() {
        return status;
    }

    @Deprecated
    public void processAllCommands() {
        if (status != HubStatus.ARMED) {
            logger.log("Hub must be armed to process commands", LogLevel.WARNING);
            return;
        }

        while (!taskQueue.isEmpty()) {
            processNextCommand();
        }
    }

    @Deprecated
    public void processCommand(Command cmd) {
        if (status != HubStatus.ARMED) {
            logger.log("Hub must be armed to process commands", LogLevel.WARNING);
            return;
        }
        cmd.execute();
    }

    @Deprecated
    public void registerAlarm(Alarm alarm) {
        activeAlarms.put(alarm.getDeviceId(), alarm);
        if (alarmRepo != null) {
            alarmRepo.save(alarm);
        } else {
            logger.log("Warning: Alarm triggered but Repository not initialized.", LogLevel.ERROR);
        }

        logger.log("Alarm registered: " + alarm.getDeviceId(), LogLevel.INFO);
    }

    public RFDataAcquisitionDeviceDetectorDevice getRfDataAcquisitionDeviceDetectorDevice() {
        return rfDataAcquisitionDeviceDetectorDevice;
    }

    @Deprecated
    public void resolveAlarm(Alarm alarm) {
        if (status != HubStatus.ARMED) {
            logger.log("Hub must be armed to resolve active alarms", LogLevel.WARNING);
            return;
        }
        alarm.resolveAlarm();
        activeAlarms.remove(alarm.getDeviceId());
    }

    @Deprecated
    public void resolveAlarmsByDevice(DataAcquisitionDevice dataAcquisitionDevice) {
        if (status != HubStatus.ARMED) {
            logger.log("Hub must be armed to resolve active alarms", LogLevel.WARNING);
            return;
        }
        activeAlarms.remove(dataAcquisitionDevice.getId());
    }

    @Deprecated
    public void resolveAllAlarms() {
        if (status != HubStatus.ARMED) {
            logger.log("Hub must be armed to resolve active alarms", LogLevel.WARNING);
            return;
        }
        activeAlarms.clear();
    }

    public void armHub() {
        if (status == HubStatus.ARMED) {
            logger.log("Hub is already armed. Current Status: ARMED", LogLevel.INFO);
        } else {
            status = HubStatus.ARMED;
            logger.log("Successfully armed hub. Current Status: ARMED", LogLevel.INFO);
        }
    }

    public void disarmHub() {
        if (status == HubStatus.DISARMED) {
            logger.log("Hub is already disarmed. Current Status: DISARMED", LogLevel.INFO);
        } else {
            status = HubStatus.DISARMED;
            logger.log("Successfully disarmed hub. Current Status: DISARMED", LogLevel.INFO);
        }
    }

    @Deprecated
    public void addCommand(SecurityHubCommand cmd) {
        if (!cmd.validate()) {
            logger.log(cmd.validationMessage(), LogLevel.ERROR);
            return;
        }

        if (cmd.getCommandType() == CommandType.RESET || cmd.getCommandType() == CommandType.DIAGNOSTIC) {
            taskQueue.add(cmd);
            return;
        }

        if (cmd.getDevice().getDeviceStatus() != DeviceStatus.OPERATIONAL) {
            logger.log("Command unsuccessful, must target an operational device.", LogLevel.ERROR);
            return;
        }

        if (status == HubStatus.DISARMED) {
            logger.log("The hub must be armed to trigger it. Current Status: DISARMED", LogLevel.WARNING);

        } else {
            if (cmd.getSeverity() != AlarmSeverity.LOW && !isMaintenanceMode) {
                taskQueue.add(cmd);
                logger.log("Successfully added security hub commands.", LogLevel.INFO);
            }
        }
    }

    @Deprecated
    /** Use this instead of individually calling addCommand and processCommand,
     * both of which are for primarily for debugging.
     */
    public void executeCommand(SecurityHubCommand cmd) {
        addCommand(cmd);
        processCommand(cmd);
    }

    @Deprecated
    public void initiateFleetCheck() {
        logger.log("Initiated fleet check. Any errors will show below.", LogLevel.HEALTH);
        for (DataAcquisitionDevice d : dataAcquisitionDevices) {
            addCommand(new SystemDiagnosticCommand(d));
        }
    }

    public int numActiveAlarms() {
        return activeAlarms.size();
    }

    public List<Subscriber> getSubscribers() {
        return subscribers;
    }

    public boolean isMaintenanceMode() {
        return isMaintenanceMode;
    }

    public boolean toggleIsMaintenanceMode() {
        this.isMaintenanceMode = !isMaintenanceMode;
        return isMaintenanceMode;
    }

    public List<Alarm> getActiveAlarms() {
        return activeAlarms.values().stream().toList();
    }

    public Map<UUID, Alarm> getActiveDevicesAndAssociatedAlarms() {
        return activeAlarms;
    }

    @Deprecated
    public void pingDevices() {
        for (DataAcquisitionDevice d : dataAcquisitionDevices) {
            d.ping();
        }
    }

    public List<DataAcquisitionDevice> getDevices() {
        return dataAcquisitionDevices;
    }

    @Deprecated
    public void checkDeviceBatteryLevels() {
        int numLowBatteriedDevices = 0;

        for (DataAcquisitionDevice d : dataAcquisitionDevices) {
            if (d.getBatteryLife() < 10) {
                logger.log(d.getId() + " is low on charge", LogLevel.HEALTH);
                numLowBatteriedDevices++;
            }
        }
        logger.log("There are " + numLowBatteriedDevices + " low batteried dataAcquisitionDevices.", LogLevel.HEALTH);
    }

    @Deprecated
    public void monitorAndHandleDeviceHealth() {
        updateDeviceFailureState();
        updateDeviceStatus();
    }

    @Deprecated
    private void updateDeviceFailureState() {
        for (DataAcquisitionDevice d : dataAcquisitionDevices) {
            if (!d.ping()) {
                d.incrementFailureCount();
                logger.log("DataAcquisitionDevice ping failed", LogLevel.WARNING);
            } else {
                d.resetFailureCount();
            }
        }
    }

    @Deprecated
    public void removeAllDevices() {
        dataAcquisitionDevices.clear();

        if (deviceRepo != null) {
            deviceRepo.removeAll();
        }
    }

    @Deprecated
    private void updateDeviceStatus() {
        List<DataAcquisitionDevice> devicesToDecommission = new ArrayList<>();

        for (DataAcquisitionDevice d : dataAcquisitionDevices) {
            int numFails = d.getFailureCount();

            if (numFails == 1 || numFails == 2) {
                logger.log("DataAcquisitionDevice failure count is " + numFails, LogLevel.WARNING);
            } else if (numFails == 3) {
                d.setDeviceStatus(DeviceStatus.RECOVERY_MODE);
                logger.log("DataAcquisitionDevice failure count is " + numFails + ". The device is in " + d.getDeviceStatus(), LogLevel.CRITICAL);
                taskQueue.addFirst(new SystemDiagnosticCommand(d));
            } else if (numFails == 4) {
                logger.log("DataAcquisitionDevice failure count is " + numFails + ". The device is in " + d.getDeviceStatus(), LogLevel.CRITICAL);
                taskQueue.addFirst(new SystemResetCommand(d));
            } else if (numFails == 5) {
                d.setDeviceStatus(DeviceStatus.DECOMMISSIONED);
                devicesToDecommission.add(d);
                decommissionedDataAcquisitionDevices.add(d);
                logger.log("DataAcquisitionDevice failure count is " + numFails + ". The device is in " + d.getDeviceStatus(), LogLevel.CRITICAL);
            }
        }
        dataAcquisitionDevices.removeIf(devicesToDecommission::contains);
    }

    @Deprecated
    public void pairNewDataAcquisitionDeviceById(UUID Id) {
        if (this.rfDataAcquisitionDeviceDetectorDevice != null) {
            rfDataAcquisitionDeviceDetectorDevice.pair(Id);
        } else {
            System.err.println("Radio Frequency DAD Detector Device doesn't exist");
        }
    }

    @Deprecated
    public void addReceiver(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    @Deprecated
    public void removeReceiver(Subscriber subscriber) {
        subscribers.remove(subscriber);
    }

    @Deprecated
    public void resetDataAcquisitionDevices() {
        for (DataAcquisitionDevice d : hub.getDevices()) {
            hub.executeCommand(new SystemResetCommand(d));
        }
    }


    public static synchronized SecurityHub getInstance() {
        if (hub == null) {
            hub = new SecurityHub();
        }
        return hub;
    }
}
