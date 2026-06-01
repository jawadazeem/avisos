/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.framework;

import com.azeem.avisos.node.config.AppConfig;
import com.azeem.avisos.node.hardware.BatteryProvider;
import com.azeem.avisos.node.network.api.MqttProvider;
import com.azeem.avisos.node.network.impl.PahoMqttProvider;
import com.azeem.avisos.node.service.HeartbeatService;
import com.azeem.avisos.node.service.NodeRuntime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lightweight IoC container for the Avisos edge node.
 *
 * <p>This is a DIY container responsible for instantiating all node components and wiring their
 * dependencies via constructor injection. It mirrors the pattern originally developed in the
 * controller service, adapted for the simpler node dependency graph.
 */
public class AppContainer {
  private final Map<Class<?>, Object> registry = new HashMap<>();

  public <T> T get(Class<T> type) {
    return type.cast(registry.get(type));
  }

  /**
   * Always use this method as opposed to directly inserting into the registry to ensure type safety.
   */
  public <T> void put(Class<? super T> type, T instance) {
    registry.put(type, instance);
  }

  public Map<Class<?>, Object> getRegistry() {
    return registry;
  }

  /** Initializes all node components in dependency order. */
  public void init() {
    // Configuration
    ConfigLoader configLoader = new ConfigLoader();
    AppConfig config = configLoader.loadAppConfig();
    put(AppConfig.class, config);

    // Hardware
    BatteryProvider batteryProvider = new BatteryProvider();
    put(BatteryProvider.class, batteryProvider);

    // Network
    MqttProvider mqttProvider = new PahoMqttProvider(config.mqtt(), config.node());
    put(MqttProvider.class, mqttProvider);

    // Serialization
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    put(ObjectMapper.class, objectMapper);

    // Services
    HeartbeatService heartbeatService =
        new HeartbeatService(
            mqttProvider, batteryProvider, config.mqtt(), config.node(), objectMapper);
    put(HeartbeatService.class, heartbeatService);

    // Runtime
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    NodeRuntime runtime =
        new NodeRuntime(config, mqttProvider, heartbeatService, batteryProvider, executor);
    put(NodeRuntime.class, runtime);
  }
}
