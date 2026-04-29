/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package sim;

import alarm.AlarmSeverity;
import commands.alarmcommands.PanicAlarmCommand;
import commands.alarmcommands.TriggerAlarmCommand;
import core.HubStatus;
import core.SecurityHub;
import devices.api.DataAcquisitionDevice;
import devices.model.DeviceStatus;
import infrastructure.logger.*;

import java.util.List;
import java.util.UUID;

public class SimulationEngine implements Runnable {
    private final SecurityHub hub;
    private final List<DataAcquisitionDevice> dataAcquisitionDevices;
    private final Logger logger;

    public SimulationEngine(SecurityHub hub, Logger logger) {
        this.hub = hub;
        dataAcquisitionDevices = hub.getDevices();
        this.logger = logger;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(10000); // The "Tick"
                lowerBatteries();
                changeSignalStrength();
                chargeBatteries();
                triggerAlarmsRandomly();
                triggerPanicAlarmsRandomly();
                // Tell the UI to refresh if needed
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * Battery charge manipulation for DADs
     */
    public void lowerBatteries() {
        dataAcquisitionDevices.forEach((d) -> {
            if (d.getBatteryLife() >= 5) {
                d.setBatteryLife(d.getBatteryLife() - (int)(Math.random() * 5));
            } else {
                d.setBatteryLife(0);
            }
        });
    }

    public void chargeBatteries() {
        dataAcquisitionDevices.forEach((d) -> {
            if (d.getBatteryLife() <= 95)
                d.setBatteryLife(d.getBatteryLife()+5);
        });
    }

    /**
     * Signal strength manipulation for DADs
     */
    public void changeSignalStrength() {
        dataAcquisitionDevices.forEach((d) -> {
            if (Math.random() > 0.5 && d.getSignalStrength() > -90) {
                d.setSignalStrength(d.getSignalStrength() - (int)(Math.random() * 10));
            } else if (d.getSignalStrength() <= -10) {
                d.setSignalStrength(d.getSignalStrength() + (int) (Math.random() * 10));
            }
        });
    }

    /**
     * Hub pairs with randomly generated device for a given UUID
     */
    public void pair(UUID Id) {

    }

    /**
     * Simulates alarms being triggered by creating and executing TriggerAlarmCommand objects randomly
     */
    public void triggerAlarmsRandomly() {
        if (SecurityHub.getInstance().currentMode() == HubStatus.ARMED) {
            dataAcquisitionDevices.stream().filter(d -> d.getDeviceStatus() == DeviceStatus.OPERATIONAL).forEach((d) -> {
                if (Math.random() > 0.9) {
                    AlarmSeverity severity;
                    double random = Math.random();
                    if (random < 0.3) {
                        severity = AlarmSeverity.LOW;
                    } else if (random > 0.6) {
                        severity = AlarmSeverity.MEDIUM;
                    } else {
                        severity = AlarmSeverity.HIGH;
                    }
                    TriggerAlarmCommand cmd = new TriggerAlarmCommand(d, "Alarm triggered", severity);
                    cmd.execute();
                }
            });
        }
    }

    /**
     * Simulates panic alarms being triggered by creating and executing PanicAlarmCommand objects randomly
     */
    public void triggerPanicAlarmsRandomly() {
        if (SecurityHub.getInstance().currentMode() == HubStatus.ARMED) {
            dataAcquisitionDevices.stream()
                    .filter(d -> d.getDeviceStatus() == DeviceStatus.OPERATIONAL)
                    .forEach((d) -> {
                        // Throttle panic generation: only a small chance per tick per device
                        if (Math.random() > 0.995) { // ~0.5% chance
                            PanicAlarmCommand cmd = new PanicAlarmCommand(d, "Panic Alarm triggered");
                            cmd.execute();
                        }
                    });
        }
    }
}
