/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.container;

import com.azeem.avisos.controller.config.*;
import com.azeem.avisos.controller.exceptions.ConfigFileMisconfiguredException;
import com.azeem.avisos.controller.exceptions.ConfigFileNotFoundException;
import com.azeem.avisos.controller.exceptions.CriticalInfrastructureException;
import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.infrastructure.cli.JLineCliClient;
import com.azeem.avisos.controller.instrumentation.annotations.*;
import com.azeem.avisos.controller.repository.AlarmRepository;
import com.azeem.avisos.controller.repository.AuthRepository;
import com.azeem.avisos.controller.repository.DeviceRepository;
import com.azeem.avisos.controller.repository.TelemetryRepository;
import com.azeem.avisos.controller.security.service.AuthService;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.command.CliService;
import com.azeem.avisos.controller.service.command.JLineCliService;
import com.azeem.avisos.controller.service.device.DeviceService;
import com.azeem.avisos.controller.service.device.SimpleDeviceService;
import com.azeem.avisos.controller.service.ingress.MqttIngressAdapter;
import com.azeem.avisos.controller.service.ingress.TelemetryIngressHandler;
import com.azeem.avisos.controller.service.notification.NotificationService;
import com.azeem.avisos.controller.service.notification.SnsService;
import com.azeem.avisos.controller.infrastructure.vision.CodeProjectVisionClient;
import com.azeem.avisos.controller.infrastructure.vision.VisionClient;
import com.azeem.avisos.controller.service.threat.KeywordThreatDetector;
import com.azeem.avisos.controller.service.threat.ThreatDetector;
import com.azeem.avisos.controller.service.vision.CodeProjectVisionService;
import com.azeem.avisos.controller.service.vision.VisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azeem.avisos.controller.repository.JdbiProvider.getJdbi;

/**
 * <p>IoC Container Class</p>
 * <p>Responsible for instantiating necessary objects and wiring their dependencies</p>
 */
public class AppContainer {
    public Map<Class<?>, Object> classObjectMap = new HashMap<>();

    public <T> T get(Class<T> type) {
        return type.cast(classObjectMap.get(type));
    }

    /**
     * Initializes all needed objects
     */
    public void init() {
        // ObjectMappers for loading all configs and for vision client
        ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory());
        ObjectMapper jsonMapper = new ObjectMapper();

        Jdbi jdbi = getJdbi(loadDBConfig(ymlMapper));

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
        AuthService authService = new AuthService(authRepository);
        AlarmService alarmService = new AlarmService(alarmRepo);
        DeviceService deviceService = new SimpleDeviceService(deviceRepo);
        VisionClient visionClient = new CodeProjectVisionClient(jsonMapper, loadVisionConfig(ymlMapper));
        VisionService visionService = new CodeProjectVisionService(visionClient);

        List<List<String>> problematicLabels = loadProblematicLabelsConfig(ymlMapper);
        ThreatDetector threatDetector = new KeywordThreatDetector(problematicLabels.get(0), problematicLabels.get(1));
        classObjectMap.put(VisionClient.class, visionService);
        classObjectMap.put(AuthService.class, authService);
        classObjectMap.put(AlarmService.class, alarmService);
        classObjectMap.put(DeviceService.class, deviceService);

        TelemetryIngressHandler telemetryIngressHandler = new TelemetryIngressHandler(
                deviceService,
                alarmService,
                visionService,
                loadVisionConfig(ymlMapper),
                threatDetector,
                new ObjectMapper()
        );
        classObjectMap.put(TelemetryIngressHandler.class, telemetryIngressHandler);

        MqttIngressAdapter mqttIngressAdapter = new MqttIngressAdapter(telemetryIngressHandler);
        classObjectMap.put(MqttIngressAdapter.class, mqttIngressAdapter);

        NotificationService notificationService = new SnsService();
        classObjectMap.put(NotificationService.class, notificationService);

        // Terminal
        CliClient cliClient = new JLineCliClient();
        CliService cliService = new JLineCliService(cliClient);
        classObjectMap.put(CliClient.class, cliClient);
        classObjectMap.put(CliService.class, cliService);
        applyAspects();
    }

    private List<List<String>> loadProblematicLabelsConfig(ObjectMapper ymlMapper) {
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException
                        ("CRITICAL: Config file not found in classpath!" +
                        " Cannot start application without security policy details."
                );
            }

            LabelConfig config = ymlMapper.readValue(is, LabelConfig.class);

            return List.of(config.critical(), config.warning());
        } catch (IOException e) {
            throw new ConfigFileNotFoundException("Failed to parse security policy", e);
        }
    }

    private VisionConfig loadVisionConfig(ObjectMapper ymlMapper) {
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException(
                        "CRITICAL: Config file not found in classpath! Cannot start "
                                + "application without vision service connection details."
                );
            }
            AppConfig config = ymlMapper.readValue(is, AppConfig.class);
            return config.getVision();
        } catch (IOException e) {
            throw new ConfigFileMisconfiguredException("Failed to parse vision policy", e);
        }
    }

    private MqttConfig loadMqttConfig(ObjectMapper ymlMapper) {
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException(
                        "CRITICAL: Config file not found in classpath!" +
                        " Cannot start application without MQTT connection details."
                );
            }
            AppConfig config = ymlMapper.readValue(is, AppConfig.class);
            return config.getMqtt();
        } catch (IOException e) {
            throw new ConfigFileMisconfiguredException("Failed to parse mqtt policy", e);
        }
    }

    private DatabaseConfig loadDBConfig(ObjectMapper ymlMapper) {
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException(
                        "CRITICAL: Config file not found in classpath! " +
                        "Cannot start application without DB connection details."
                );
            }
            AppConfig config = ymlMapper.readValue(is, AppConfig.class);
            return config.getDatabase();
        } catch (IOException e) {
            throw new ConfigFileMisconfiguredException("Failed to parse database policy", e);
        }
    }

    private <T> void applyAspects() {
        ServiceAuditAspect();
        TimedAspect();
    }

    // TODO: Use terminal logging instead of System.out.println and make it more elegant
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
