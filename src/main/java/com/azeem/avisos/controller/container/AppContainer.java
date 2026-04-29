/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.container;

import com.azeem.avisos.controller.repository.AlarmRepository;
import com.azeem.avisos.controller.repository.AuthRepository;
import com.azeem.avisos.controller.repository.DeviceRepository;
import com.azeem.avisos.controller.repository.TelemetryRepository;
import com.azeem.avisos.controller.service.device.DeviceService;
import com.azeem.avisos.controller.service.device.DeviceServiceImpl;
import com.azeem.avisos.controller.service.mqtt.MqttService;
import com.azeem.avisos.node.devices.api.DataAcquisitionDevice;
import com.azeem.avisos.node.devices.impl.RFDataAcquisitionDeviceDetectorDevice;
import com.azeem.avisos.controller.instrumentation.annotations.ServiceAudit;
import com.azeem.avisos.controller.instrumentation.annotations.Timed;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

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
        Jdbi jdbi = databaseConfiguration();

        // Repositories
        AlarmRepository alarmRepo = jdbi.onDemand(AlarmRepository.class);
        DeviceRepository deviceRepo = jdbi.onDemand(DeviceRepository.class);
        TelemetryRepository telemetryRepository = jdbi.onDemand(TelemetryRepository.class);
        AuthRepository authRepository = jdbi.onDemand(AuthRepository.class);
        classObjectMap.put(AlarmRepository.class, alarmRepo);
        classObjectMap.put(DeviceRepository.class, deviceRepo);
        classObjectMap.put(TelemetryRepository.class, telemetryRepository);
        classObjectMap.put(AuthRepository.class, authRepository);

        // Services
        AlarmService alarmService = new AlarmService(alarmRepo);
        DeviceService deviceService = new DeviceServiceImpl(deviceRepo);
        MqttService mqttService = new MqttService(deviceService, alarmService);
        classObjectMap.put(AlarmService.class, alarmService);
        classObjectMap.put(DeviceService.class, deviceService);
        classObjectMap.put(MqttService.class, mqttService);
    }

    private Jdbi databaseConfiguration() {
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:avisos.db");
        jdbi.installPlugin(new SqlObjectPlugin());
        return jdbi;
    }

    /**
     * Wires dependencies via setter injection
     */
    private void wireDependencies() {

    }

    public <T> void applyAspects() {
        ServiceAuditAspect();
        TimedAspect();
    }

    private void ServiceAuditAspect() {
        System.out.println("\n--- [ Avisos Audit Scan ] ---");

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
}
