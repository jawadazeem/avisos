/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.framework;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.infrastructure.cli.JLineCliClient;
import com.azeem.avisos.controller.infrastructure.cli.command.InMemoryCommandRegistry;
import com.azeem.avisos.controller.infrastructure.health.DatabaseHealthCheck;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor;
import com.azeem.avisos.controller.infrastructure.ingress.MqttIngressListener;
import com.azeem.avisos.controller.infrastructure.lifecycle.ShutdownManager;
import com.azeem.avisos.controller.infrastructure.vision.CodeProjectVisionClient;
import com.azeem.avisos.controller.infrastructure.vision.VisionClient;
import com.azeem.avisos.controller.repository.AlarmRepository;
import com.azeem.avisos.controller.repository.NodeRepository;
import com.azeem.avisos.controller.repository.TelemetryRepository;
import com.azeem.avisos.controller.security.model.SecurityContext;
import com.azeem.avisos.controller.security.repository.AuthRepository;
import com.azeem.avisos.controller.security.service.AuthService;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.cli.CliService;
import com.azeem.avisos.controller.service.cli.JLineCliService;
import com.azeem.avisos.controller.infrastructure.cli.command.CommandRegistry;
import com.azeem.avisos.controller.service.cli.command.impl.*;
import com.azeem.avisos.controller.service.ingress.MqttIngressAdapter;
import com.azeem.avisos.controller.service.ingress.TelemetryIngressHandler;
import com.azeem.avisos.controller.service.node.NodeService;
import com.azeem.avisos.controller.service.node.SimpleNodeService;
import com.azeem.avisos.controller.service.notification.NotificationService;
import com.azeem.avisos.controller.service.notification.SnsService;
import com.azeem.avisos.controller.service.threat.KeywordThreatDetector;
import com.azeem.avisos.controller.service.threat.ThreatDetector;
import com.azeem.avisos.controller.service.vision.CodeProjectVisionService;
import com.azeem.avisos.controller.service.vision.VisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.jdbi.v3.core.Jdbi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.azeem.avisos.controller.repository.JdbiProvider.getDataSource;
import static com.azeem.avisos.controller.repository.JdbiProvider.getJdbi;

/**
 * <p>IoC Container Class</p>
 * <p>
 *     This is a DIY Spring Container responsible for instantiating necessary objects and wiring
 *     their dependencies
 * </p>
 */
public class AppContainer {
    private final Map<Class<?>, Object> classObjectRegistry = new HashMap<>();

    public <T> T get(Class<T> type) {
        return type.cast(classObjectRegistry.get(type));
    }

    /**
     * Always use this method as opposed to directly inserting into the classObjectRegistry
     * to ensure type safety.
     */
    public <T> void put(Class<? super T> type, T instance) {
        classObjectRegistry.put(type, instance);
    }

    public Map<Class<?>, Object> getClassObjectRegistry() {
        return classObjectRegistry;
    }

    /**
     * Initializes all needed objects
     */
    public void init() {
        // Executor
        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();

        ExecutorService virtualWorkers =
                Executors.newVirtualThreadPerTaskExecutor();

        ConfigLoader configLoader = new ConfigLoader();

        Jdbi jdbi = getJdbi(configLoader.loadDBConfig());

        // Repositories
        AlarmRepository alarmRepo = jdbi.onDemand(AlarmRepository.class);
        NodeRepository nodeRepo = jdbi.onDemand(NodeRepository.class);
        TelemetryRepository telemetryRepository = jdbi.onDemand(TelemetryRepository.class);
        AuthRepository authRepository = jdbi.onDemand(AuthRepository.class);
        put(AlarmRepository.class, alarmRepo);
        put(NodeRepository.class, nodeRepo);
        put(TelemetryRepository.class, telemetryRepository);
        put(AuthRepository.class, authRepository);

        alarmRepo.initAlarmTable();
        nodeRepo.initNodeTable();
        telemetryRepository.initAuditTable();
        authRepository.initUserTable();

        // Services
        AuthService authService = new AuthService(authRepository);
        AlarmService alarmService = new AlarmService(alarmRepo);
        NodeService nodeService = new SimpleNodeService(
                nodeRepo,
                configLoader.loadNodeServiceConfig());

        VisionClient visionClient = new CodeProjectVisionClient(
                new ObjectMapper(),
                configLoader.loadVisionConfig()
        );

        VisionService visionService = new CodeProjectVisionService(visionClient);

        List<List<String>> problematicLabels = configLoader.loadProblematicLabelsConfig();
        ThreatDetector threatDetector = new KeywordThreatDetector(problematicLabels.get(0), problematicLabels.get(1));
        put(VisionClient.class, visionClient);
        put(VisionService.class, visionService);
        put(AuthService.class, authService);
        put(AlarmService.class, alarmService);
        put(NodeService.class, nodeService);

        TelemetryIngressHandler telemetryIngressHandler = new TelemetryIngressHandler(
                nodeService,
                alarmService,
                visionService,
                configLoader.loadVisionConfig(),
                threatDetector,
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .registerModule(new ParameterNamesModule())
        );
        put(TelemetryIngressHandler.class, telemetryIngressHandler);

        MqttIngressAdapter mqttIngressAdapter = new MqttIngressAdapter(telemetryIngressHandler);
        put(MqttIngressAdapter.class, mqttIngressAdapter);

        MqttIngressListener mqttIngressListener = new MqttIngressListener(
                mqttIngressAdapter,
                configLoader.loadMqttConfig()
        );
        mqttIngressListener.init();
        put(MqttIngressListener.class, mqttIngressListener);

        NotificationService notificationService = new SnsService();
        put(NotificationService.class, notificationService);

        // Auth context (must be singleton)
        SecurityContext securityContext = new SecurityContext();

        // Terminal
        CliClient cliClient = new JLineCliClient();
        put(CliClient.class, cliClient);

        // Register Commands
        CommandRegistry commandRegistry = new InMemoryCommandRegistry();
        put(CommandRegistry.class, commandRegistry);
        commandRegistry.register(new ExitCommand(cliClient));
        commandRegistry.register(new AlarmsCommand(cliClient, alarmService));
        commandRegistry.register(new HelpCommand(cliClient, commandRegistry));
        commandRegistry.register(new InspectCommand(cliClient, nodeService));
        commandRegistry.register(new NodesCommand(cliClient, nodeService));
        commandRegistry.register(new PurgeCommand(cliClient, nodeService));
        commandRegistry.register(new StatsCommand(cliClient));


        // Rest of the terminal
        CliService cliService = new JLineCliService(
                cliClient,
                commandRegistry,
                authService,
                securityContext,
                Executors.newVirtualThreadPerTaskExecutor());

        Thread cliThread = new Thread(cliService::start);
        cliThread.setDaemon(false);  // Non-daemon keeps JVM alive
        cliThread.setName("CLI-Thread");
        cliThread.start();
        put(CliService.class, cliService);

        DatabaseHealthCheck dbHealthCheck = new DatabaseHealthCheck(getDataSource());
        SystemHealthMonitor systemHealthMonitor =
                new SystemHealthMonitor(dbHealthCheck, virtualWorkers);

        // Scheduling
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                systemHealthMonitor.checkSystemHealth();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.SECONDS);

        // Application Shutdown

        ShutdownManager shutdownManager = new ShutdownManager();
        put(ShutdownManager.class, shutdownManager);

        shutdownManager.addTask(() -> {
            try {
                scheduler.shutdown();
                if (!virtualWorkers.awaitTermination(5, TimeUnit.SECONDS)) {
                    virtualWorkers.shutdownNow();
                }
            } catch (InterruptedException e) {
                virtualWorkers.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }).addTask(() -> {
            try {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        });
    }
}
