/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.framework;

import com.azeem.avisos.controller.infrastructure.cli.*;
import com.azeem.avisos.controller.infrastructure.ingress.MqttIngressListener;
import com.azeem.avisos.controller.repository.*;
import com.azeem.avisos.controller.security.repository.AuthRepository;
import com.azeem.avisos.controller.security.service.AuthService;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.cli.CliService;
import com.azeem.avisos.controller.service.cli.*;
import com.azeem.avisos.controller.service.cli.command.api.CommandRegistry;
import com.azeem.avisos.controller.service.cli.command.impl.AlarmsCommand;
import com.azeem.avisos.controller.service.cli.command.impl.ExitCommand;
import com.azeem.avisos.controller.service.cli.command.impl.InMemoryCommandRegistry;
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
import org.jdbi.v3.core.Jdbi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.azeem.avisos.controller.repository.JdbiProvider.getJdbi;

/**
 * <p>IoC Container Class</p>
 * <p>
 *     This is a DIY Spring Container responsible for instantiating necessary objects and wiring
 *     their dependencies
 * </p>
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

        ConfigLoader configLoader = new ConfigLoader();

        Jdbi jdbi = getJdbi(configLoader.loadDBConfig());

        // Repositories
        AlarmRepository alarmRepo = jdbi.onDemand(AlarmRepository.class);
        DeviceRepository deviceRepo = jdbi.onDemand(DeviceRepository.class);
        TelemetryRepository telemetryRepository = jdbi.onDemand(TelemetryRepository.class);
        AuthRepository authRepository = jdbi.onDemand(AuthRepository.class);
        classObjectMap.put(AlarmRepository.class, alarmRepo);
        classObjectMap.put(DeviceRepository.class, deviceRepo);
        classObjectMap.put(TelemetryRepository.class, telemetryRepository);
        classObjectMap.put(AuthRepository.class, authRepository);

        alarmRepo.initAlarmTable();
        deviceRepo.initDeviceTable();
        telemetryRepository.initAuditTable();
        authRepository.initUserTable();

        // Services
        AuthService authService = new AuthService(authRepository);
        AlarmService alarmService = new AlarmService(alarmRepo);
        DeviceService deviceService = new SimpleDeviceService(deviceRepo);
        VisionClient visionClient = new CodeProjectVisionClient(
                new ObjectMapper(),
                configLoader.loadVisionConfig()
        );
        VisionService visionService = new CodeProjectVisionService(visionClient);

        List<List<String>> problematicLabels = configLoader.loadProblematicLabelsConfig();
        ThreatDetector threatDetector = new KeywordThreatDetector(problematicLabels.get(0), problematicLabels.get(1));
        classObjectMap.put(VisionClient.class, visionService);
        classObjectMap.put(AuthService.class, authService);
        classObjectMap.put(AlarmService.class, alarmService);
        classObjectMap.put(DeviceService.class, deviceService);

        TelemetryIngressHandler telemetryIngressHandler = new TelemetryIngressHandler(
                deviceService,
                alarmService,
                visionService,
                configLoader.loadVisionConfig(),
                threatDetector,
                new ObjectMapper()
        );
        classObjectMap.put(TelemetryIngressHandler.class, telemetryIngressHandler);

        MqttIngressAdapter mqttIngressAdapter = new MqttIngressAdapter(telemetryIngressHandler);
        classObjectMap.put(MqttIngressAdapter.class, mqttIngressAdapter);

        MqttIngressListener mqttIngressListener = new MqttIngressListener(
                mqttIngressAdapter,
                configLoader.loadMqttConfig()
        );
        // TODO: Uncomment
//        mqttIngressListener.init();
        classObjectMap.put(MqttIngressListener.class, mqttIngressListener);

        NotificationService notificationService = new SnsService();
        classObjectMap.put(NotificationService.class, notificationService);


        // Terminal
        CliClient cliClient = new JLineCliClient();
        classObjectMap.put(CliClient.class, cliClient);

        // Register Commands
        CommandRegistry commandRegistry = new InMemoryCommandRegistry();
        classObjectMap.put(CommandRegistry.class, commandRegistry);
        commandRegistry.register(new ExitCommand(cliClient));
        commandRegistry.register(new AlarmsCommand(cliClient, alarmService));

        ExecutorService cliExecutor = Executors.newVirtualThreadPerTaskExecutor();
        CliService cliService = new JLineCliService(cliClient, commandRegistry, cliExecutor);
        cliExecutor.submit(cliService::runCommand);
        classObjectMap.put(CliService.class, cliService);
    }
}
