/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.container;

import com.azeem.avisos.alarm.Alarm;
import com.azeem.avisos.core.SecurityHub;
import com.azeem.avisos.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.devices.impl.RFDataAcquisitionDeviceDetectorDevice;
import com.azeem.avisos.infrastructure.annotations.ServiceAudit;
import com.azeem.avisos.infrastructure.annotations.Timed;
import com.azeem.avisos.infrastructure.logger.LogFileArchiver;
import com.azeem.avisos.infrastructure.logger.Logger;
import com.azeem.avisos.infrastructure.repository.*;
import com.azeem.avisos.infrastructure.subscribers.Subscriber;
import com.azeem.avisos.service.alarm.AlarmService;
import com.azeem.avisos.service.hub.PerformanceHandler;
import com.azeem.avisos.service.hub.api.SecurityHubService;
import com.azeem.avisos.service.hub.impl.SecurityHubServiceImpl;
import com.azeem.avisos.service.receiver.ReceiverService;
import com.azeem.avisos.service.system.SystemHealthService;
import com.azeem.avisos.sim.DirectConnectDXHardwareLinkSim;
import com.azeem.avisos.sim.RFHardwareLinkSim;
import com.azeem.avisos.sim.SimulationEngine;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * <h2>IoC Container Class</h2>
 * <p>Responsible for instantiating necessary objects and wiring their dependencies</p>
 */
public class AppContainer {
    Map<Class<?>, Object> classObjectMap = new HashMap<>();

    public <T> T get(Class<T> type) {
        return type.cast(classObjectMap.get(type));
    }

    /**
     * Initializes all needed objects
     */
    public void init() {
        SecurityHub hub = SecurityHub.getInstance();
        setupLogging(hub.getLogger());
        hubReboot(hub);

        classObjectMap.put(SecurityHub.class, hub);

        // Replace with real hardware links in prod
        SimulationEngine hardwareLink = new SimulationEngine(hub, hub.getLogger());
        RFHardwareLinkSim rfHardwareLink = new RFHardwareLinkSim(hub, hub.getLogger());
        DirectConnectDXHardwareLinkSim directConnectDXHardwareLink = new DirectConnectDXHardwareLinkSim(hub.getLogger());
        RFDataAcquisitionDeviceDetectorDevice rfDataAcquisitionDeviceDetectorDevice = new RFDataAcquisitionDeviceDetectorDevice(hub, rfHardwareLink);
        hub.setRfDataAcquisitionDeviceDetectorDevice(rfDataAcquisitionDeviceDetectorDevice);

        classObjectMap.put(SimulationEngine.class, hardwareLink);
        classObjectMap.put(RFHardwareLinkSim.class, rfHardwareLink);
        classObjectMap.put(DirectConnectDXHardwareLinkSim.class, rfHardwareLink);
        classObjectMap.put(RFDataAcquisitionDeviceDetectorDevice.class, rfDataAcquisitionDeviceDetectorDevice);

        DatabaseManager databaseManager = DatabaseManager.getInstance();

        // Repositories
        DeviceRepository dRepo = new DeviceRepository(hub.getLogger(), rfHardwareLink);
        AlarmLogRepository aRepo = new AlarmLogRepository(hub.getLogger());
        UserRepository uRepo = new UserRepository(hub.getLogger());
        SubscriberRepository sRepo = new SubscriberRepository(hub.getLogger());
        hub.setAlarmRepository(aRepo);
        hub.setDeviceRepo(dRepo);
        classObjectMap.put(DeviceRepository.class, dRepo);
        classObjectMap.put(AlarmLogRepository.class, aRepo);
        classObjectMap.put(UserRepository.class, uRepo);
        classObjectMap.put(SubscriberRepository.class, sRepo);

        rehydrateHub(hub);

        // Service Layer/Business Logic
        SystemHealthService systemHealthSvc = new SystemHealthService(hub);
        SecurityHubService hubService = new SecurityHubServiceImpl(hub, hub.getLogger(), rfDataAcquisitionDeviceDetectorDevice, aRepo, dRepo);
        PerformanceHandler handler = new PerformanceHandler(hubService);

        SecurityHubService proxy = (SecurityHubService) Proxy.newProxyInstance(
                SecurityHubService.class.getClassLoader(),
                new Class[] { SecurityHubService.class },
                handler
        );

        ReceiverService receiverService = new ReceiverService(hub, sRepo, directConnectDXHardwareLink, hub.getLogger());
        AlarmService alarmService = new AlarmService(aRepo, hub.getLogger());
        classObjectMap.put(SystemHealthService.class, systemHealthSvc);
        classObjectMap.put(SecurityHubService.class, proxy);
        classObjectMap.put(ReceiverService.class, receiverService);
        classObjectMap.put(AlarmService.class, alarmService);

        // Helper methods
        wireDependencies(hub, directConnectDXHardwareLink, receiverService);
        addShutdownHook(dRepo, aRepo, sRepo);
    }

    /**
     * Wires dependencies via setter injection
     */
    private void wireDependencies(SecurityHub hub, DirectConnectDXHardwareLinkSim dXHardwareLinkSim, ReceiverService receiverService) {
        dXHardwareLinkSim.setReceiverService(receiverService);
    }

    private static void addShutdownHook(DeviceRepository dRepo, AlarmLogRepository aRepo, SubscriberRepository sRepo) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down Sentinel Application. Saving Application Data & State.");

            List<DataAcquisitionDevice> currentFleet = SecurityHub.getInstance().getDevices();
            System.out.println("Saving " + currentFleet.size() + " com.azeem.avisos.devices...");
            for (DataAcquisitionDevice d : currentFleet) {
                dRepo.save(d);
            }

            List<Alarm> currentActiveAlarms = SecurityHub.getInstance().getActiveAlarms();
            for (Alarm a : currentActiveAlarms) {
                aRepo.save(a);
            }

            List<Subscriber> subscribers = SecurityHub.getInstance().getSubscribers();
            for (Subscriber s : subscribers) {
                sRepo.save(s);
            }

            DatabaseManager.getInstance().closeConnection();
        }));
    }

    public <T> void applyAspects() {
        ServiceAuditAspect();
        TimedAspect();
    }

    private void ServiceAuditAspect() {
        System.out.println("\n--- [ Sentinel Security Audit Scan ] ---");

        for (Object instance : classObjectMap.values()) {
            Class<?> clazz = instance.getClass();

            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(ServiceAudit.class)) {
                    ServiceAudit audit = m.getAnnotation(ServiceAudit.class);

                    String actionName = audit.value().isEmpty() ? m.getName() : audit.value();
                    System.out.println("[AUDIT POINT] Verified: " + clazz.getSimpleName()
                            + " -> " + m.getName()
                            + " (Context: " + actionName + ")");
                }
            }
        }
        System.out.println("--- [ Audit Scan Complete ] ---\n");
    }

    private void TimedAspect() {
        for (Object instance : classObjectMap.values()) {
            Class<?> clazz = instance.getClass();

            for (Method m : clazz.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Timed.class)) {
                    Timed timed = m.getAnnotation(Timed.class);

                    System.out.println("[PERFORMANCE MONITOR]: Watching: "
                            + clazz.getSimpleName()
                            + " -> " + m.getName() + " Threshold (50 ms)");
                }
            }
        }
    }

    /**
     * Not necessary in prod, for testing.
     */
    private void hubReboot(SecurityHub hub) {
        hub.armHub();
        hub.initiateFleetCheck();
        hub.monitorAndHandleDeviceHealth();
        hub.processAllCommands();
    }

    /**
     * Rehydrate the hub's com.azeem.avisos.devices in memory, looping through the DB.
     */
    private void rehydrateHub(SecurityHub hub) {
        List<DataAcquisitionDevice> savedDataAcquisitionDevices = hub.getDeviceRepo().loadAll();
        for(DataAcquisitionDevice d : savedDataAcquisitionDevices) {
            hub.addDevice(d);
        }
    }

    private static void setupLogging(Logger logger) {
        logger.registerListener(new LogFileArchiver("logs.txt"));
    }
}
