/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.service;

import com.azeem.avisos.node.config.AppConfig;
import com.azeem.avisos.node.hardware.BatteryProvider;
import com.azeem.avisos.node.model.node.State;
import com.azeem.avisos.node.network.api.MqttProvider;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates the node process lifecycle.
 *
 * <p>Runtime work is performed on virtual threads so the edge process can keep startup non-blocking
 * while still running a resilient connection supervisor, telemetry heartbeat loop, and watchdog.
 */
public class NodeRuntime {
  private static final Logger log = LoggerFactory.getLogger(NodeRuntime.class);

  private static final Duration DEFAULT_HEARTBEAT_INTERVAL = Duration.ofSeconds(30);
  private static final Duration DEFAULT_WATCHDOG_INTERVAL = Duration.ofSeconds(60);
  private static final Duration DEFAULT_INITIAL_RETRY_DELAY = Duration.ofSeconds(1);
  private static final Duration DEFAULT_MAX_RETRY_DELAY = Duration.ofMinutes(1);
  private static final int LOW_BATTERY_THRESHOLD = 15;

  private final AppConfig config;
  private final MqttProvider mqttProvider;
  private final HeartbeatService heartbeatService;
  private final BatteryProvider batteryProvider;
  private final Duration heartbeatInterval;
  private final Duration watchdogInterval;
  private final Duration initialRetryDelay;
  private final Duration maxRetryDelay;
  private final AtomicReference<State> state = new AtomicReference<>(State.SHUTDOWN);

  private ExecutorService executor;

  // TODO: Update NodeRuntime to use ReactiveBufferManager, not directly PahoMqttProvider
  public NodeRuntime(
      AppConfig config,
      MqttProvider mqttProvider,
      HeartbeatService heartbeatService,
      BatteryProvider batteryProvider,
      ExecutorService executor) {
    this(
        config,
        mqttProvider,
        heartbeatService,
        batteryProvider,
        executor,
        DEFAULT_HEARTBEAT_INTERVAL,
        DEFAULT_WATCHDOG_INTERVAL,
        DEFAULT_INITIAL_RETRY_DELAY,
        DEFAULT_MAX_RETRY_DELAY);
  }

  NodeRuntime(
      AppConfig config,
      MqttProvider mqttProvider,
      HeartbeatService heartbeatService,
      BatteryProvider batteryProvider,
      ExecutorService executor,
      Duration heartbeatInterval,
      Duration watchdogInterval,
      Duration initialRetryDelay,
      Duration maxRetryDelay) {
    this.config = Objects.requireNonNull(config, "config");
    this.mqttProvider = Objects.requireNonNull(mqttProvider, "mqttProvider");
    this.heartbeatService = Objects.requireNonNull(heartbeatService, "heartbeatService");
    this.batteryProvider = Objects.requireNonNull(batteryProvider, "batteryProvider");
    this.executor = Objects.requireNonNull(executor, "executor");
    this.heartbeatInterval = positive(heartbeatInterval, "heartbeatInterval");
    this.watchdogInterval = positive(watchdogInterval, "watchdogInterval");
    this.initialRetryDelay = positive(initialRetryDelay, "initialRetryDelay");
    this.maxRetryDelay = positive(maxRetryDelay, "maxRetryDelay");
  }

  /** Starts runtime supervision without blocking the caller. */
  public void start() {
    if (!state.compareAndSet(State.SHUTDOWN, State.STARTING)) {
      log.warn("Node runtime start ignored: current state={}", state.get());
      return;
    }

    logStartupConfiguration();

    executor.submit(this::runConnectionSupervisor);
    executor.submit(this::runWatchdog);
  }

  /** Stops background work and disconnects from MQTT. */
  public void stop() {
    State current = state.get();
    if (current == State.SHUTDOWN || current == State.STOPPING) {
      return;
    }

    state.set(State.STOPPING);
    log.info("Stopping Avisos node runtime");

    if (executor != null) {
      executor.shutdownNow();
    }

    try {
      mqttProvider.disconnect();
    } catch (RuntimeException e) {
      log.warn("MQTT disconnect failed during shutdown: {}", e.getMessage());
    }

    awaitExecutorShutdown();
    state.set(State.SHUTDOWN);
    log.info("Avisos node runtime stopped");
  }

  public State state() {
    return state.get();
  }

  private void runConnectionSupervisor() {
    Duration retryDelay = initialRetryDelay;

    while (isOperational()) {
      try {
        log.info(
            "Connecting to MQTT broker: brokerUrl={}, topic={}",
            config.mqtt().brokerUrl(),
            config.mqtt().topic());
        mqttProvider.connect();
        state.set(State.RUNNING);
        log.info("MQTT connection established; heartbeat loop enabled");

        runHeartbeatLoop();
        retryDelay = initialRetryDelay;
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      } catch (Exception e) {
        if (!isOperational()) {
          return;
        }

        state.set(State.STARTING);
        log.warn(
            "MQTT broker unreachable: {}. Retrying in {} ms",
            e.getMessage(),
            retryDelay.toMillis());

        if (!sleep(retryDelay)) {
          return;
        }
        retryDelay = nextRetryDelay(retryDelay);
      }
    }
  }

  private void runHeartbeatLoop() throws InterruptedException {
    while (state.get() == State.RUNNING) {
      heartbeatService.sendTelemetry();
      TimeUnit.MILLISECONDS.sleep(heartbeatInterval.toMillis());
    }
  }

  private void runWatchdog() {
    while (isOperational()) {
      try {
        int batteryLevel = batteryProvider.getBatteryLevel();
        State currentState = state.get();

        if (batteryLevel <= LOW_BATTERY_THRESHOLD) {
          log.warn("R-Team watchdog: low battery detected, level={}%", batteryLevel);
        }

        if (currentState == State.STARTING) {
          log.warn("R-Team watchdog: MQTT connectivity not yet established");
        } else {
          log.debug("R-Team watchdog: state={}, batteryLevel={}%", currentState, batteryLevel);
        }
      } catch (RuntimeException e) {
        log.warn("R-Team watchdog: hardware vitals check failed: {}", e.getMessage());
      }

      if (!sleep(watchdogInterval)) {
        return;
      }
    }
  }

  private void logStartupConfiguration() {
    log.info(
        "Starting Avisos node: nodeId={}, name={}, type={}, mqttBroker={}, mqttTopic={}",
        config.node().nodeId(),
        config.node().name(),
        config.node().type(),
        config.mqtt().brokerUrl(),
        config.mqtt().topic());
  }

  private Duration nextRetryDelay(Duration currentDelay) {
    long nextMillis = currentDelay.toMillis() * 2;
    return Duration.ofMillis(Math.min(nextMillis, maxRetryDelay.toMillis()));
  }

  private boolean isOperational() {
    State current = state.get();
    return current == State.STARTING || current == State.RUNNING;
  }

  private boolean sleep(Duration duration) {
    try {
      TimeUnit.MILLISECONDS.sleep(duration.toMillis());
      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private void awaitExecutorShutdown() {
    if (executor == null) {
      return;
    }

    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        log.warn("Runtime virtual threads did not stop before timeout");
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  private static Duration positive(Duration duration, String name) {
    Objects.requireNonNull(duration, name);
    if (duration.isZero() || duration.isNegative()) {
      throw new IllegalArgumentException(name + " must be positive");
    }
    return duration;
  }
}
