/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.framework;

import com.azeem.avisos.controller.infrastructure.cli.*;
import com.azeem.avisos.controller.infrastructure.ingress.MqttIngressListener;
import com.azeem.avisos.controller.repository.*;
import com.azeem.avisos.controller.security.model.SecurityContext;
import com.azeem.avisos.controller.security.repository.AuthRepository;
import com.azeem.avisos.controller.security.service.AuthService;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.cli.CliService;
import com.azeem.avisos.controller.service.cli.*;
import com.azeem.avisos.controller.service.cli.command.api.Command;
import com.azeem.avisos.controller.service.cli.command.api.CommandRegistry;
import com.azeem.avisos.controller.service.cli.command.impl.*;
import com.azeem.avisos.controller.service.node.NodeService;
import com.azeem.avisos.controller.service.node.SimpleNodeService;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.jdbi.v3.core.Jdbi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        NodeRepository nodeRepo = jdbi.onDemand(NodeRepository.class);
        TelemetryRepository telemetryRepository = jdbi.onDemand(TelemetryRepository.class);
        AuthRepository authRepository = jdbi.onDemand(AuthRepository.class);
        classObjectMap.put(AlarmRepository.class, alarmRepo);
        classObjectMap.put(NodeRepository.class, nodeRepo);
        classObjectMap.put(TelemetryRepository.class, telemetryRepository);
        classObjectMap.put(AuthRepository.class, authRepository);

        alarmRepo.initAlarmTable();
        nodeRepo.initNodeTable();
        telemetryRepository.initAuditTable();
        authRepository.initUserTable();

        // Services
        AuthService authService = new AuthService(authRepository);
        AlarmService alarmService = new AlarmService(alarmRepo);
        NodeService nodeService = new SimpleNodeService(nodeRepo);
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
        classObjectMap.put(NodeService.class, nodeService);

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

        // Auth context (must be singleton)
        SecurityContext securityContext = new SecurityContext();

        // Terminal
        CliClient cliClient = new JLineCliClient();
        classObjectMap.put(CliClient.class, cliClient);

        // Register Commands (Non-Secure)
        CommandRegistry commandRegistry = new InMemoryCommandRegistry();
        classObjectMap.put(CommandRegistry.class, commandRegistry);
        commandRegistry.register(new ExitCommand(cliClient));

        Command alarmsCmd = new AlarmsCommand(cliClient, alarmService);
        commandRegistry.register(alarmsCmd);
        classObjectMap.put(AlarmsCommand.class, alarmsCmd);

        Command helpCmd = new HelpCommand(cliClient, commandRegistry);
        commandRegistry.register(helpCmd);
        classObjectMap.put(HelpCommand.class, helpCmd);

        Command inspectCmd = new InspectCommand(cliClient, nodeService);
        commandRegistry.register(inspectCmd);
        classObjectMap.put(InspectCommand.class, inspectCmd);

        Command nodesCmd = new NodesCommand(cliClient, nodeService);
        commandRegistry.register(nodesCmd);
        classObjectMap.put(NodesCommand.class, nodesCmd);

        Command purgeCmd = new PurgeCommand(cliClient, nodeService);
        commandRegistry.register(purgeCmd);
        classObjectMap.put(PurgeCommand.class, purgeCmd);

        Command statsCmd = new StatsCommand(cliClient);
        commandRegistry.register(statsCmd);
        classObjectMap.put(StatsCommand.class, statsCmd);

        // Rest of the terminal
        CliService cliService = new JLineCliService(
                cliClient,
                commandRegistry,
                authService,
                securityContext);

        Thread cliThread = new Thread(cliService::start);
        cliThread.setDaemon(false);  // Non-daemon keeps JVM alive
        cliThread.setName("CLI-Thread");
        cliThread.start();
        classObjectMap.put(CliService.class, cliService);
    }
}
