/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azeem.avisos.node.config.AppConfig;
import com.azeem.avisos.node.config.HardwareConfig;
import com.azeem.avisos.node.config.MqttConfig;
import com.azeem.avisos.node.config.NodeConfig;
import com.azeem.avisos.node.hardware.HardwareSnapshot;
import com.azeem.avisos.node.hardware.HardwareTelemetryProvider;
import com.azeem.avisos.node.model.node.State;
import com.azeem.avisos.node.network.api.MqttProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

public class NodeRuntimeTest {

  @Test
  void startShouldReturnWithoutBlockingAndBeginHeartbeatAfterMqttConnect() throws Exception {
    TestMqttProvider mqttProvider = new TestMqttProvider();
    CountingHeartbeatService heartbeatService = heartbeatService(mqttProvider);
    NodeRuntime runtime = runtime(mqttProvider, heartbeatService);

    runtime.start();

    assertTrue(mqttProvider.connected.await(1, TimeUnit.SECONDS));
    assertTrue(heartbeatService.sent.await(1, TimeUnit.SECONDS));
    assertEquals(State.RUNNING, runtime.state());

    runtime.stop();

    assertEquals(State.SHUTDOWN, runtime.state());
    assertEquals(1, mqttProvider.disconnects.get());
  }

  @Test
  void startShouldRetryMqttConnectionWithBackoff() throws Exception {
    TestMqttProvider mqttProvider = new TestMqttProvider();
    mqttProvider.failuresBeforeSuccess.set(2);
    CountingHeartbeatService heartbeatService = heartbeatService(mqttProvider);
    NodeRuntime runtime = runtime(mqttProvider, heartbeatService);

    runtime.start();

    assertTrue(heartbeatService.sent.await(1, TimeUnit.SECONDS));
    assertEquals(3, mqttProvider.connectAttempts.get());

    runtime.stop();
  }

  private static NodeRuntime runtime(MqttProvider mqttProvider, HeartbeatService heartbeatService) {
    return new NodeRuntime(
        appConfig(),
        mqttProvider,
        heartbeatService,
        new FixedHardwareTelemetryProvider(),
        Executors.newVirtualThreadPerTaskExecutor(),
        Duration.ofMillis(10),
        Duration.ofMillis(10),
        Duration.ofMillis(10),
        Duration.ofMillis(20));
  }

  private static CountingHeartbeatService heartbeatService(MqttProvider mqttProvider) {
    AppConfig config = appConfig();
    return new CountingHeartbeatService(
        mqttProvider,
        new FixedHardwareTelemetryProvider(),
        config.mqtt(),
        config.node(),
        new ObjectMapper());
  }

  private static AppConfig appConfig() {
    return new AppConfig(
        new NodeConfig(
            UUID.fromString("00000000-0000-0000-0000-000000000000"), "test-node", "sensor"),
        new MqttConfig("tcp://localhost:1883", "avisos/test"),
        new HardwareConfig("local", "http://localhost:5000", Duration.ofMillis(100)));
  }

  private static final class CountingHeartbeatService extends HeartbeatService {
    private final CountDownLatch sent = new CountDownLatch(1);

    private CountingHeartbeatService(
        MqttProvider mqttProvider,
        HardwareTelemetryProvider hardwareTelemetryProvider,
        MqttConfig mqttConfig,
        NodeConfig nodeConfig,
        ObjectMapper objectMapper) {
      super(mqttProvider, hardwareTelemetryProvider, mqttConfig, nodeConfig, objectMapper);
    }

    @Override
    public void sendTelemetry() {
      sent.countDown();
    }
  }

  private static final class FixedHardwareTelemetryProvider implements HardwareTelemetryProvider {
    @Override
    public HardwareSnapshot readSnapshot() {
      return HardwareSnapshot.localBattery(80);
    }
  }

  private static final class TestMqttProvider implements MqttProvider {
    private final AtomicInteger connectAttempts = new AtomicInteger();
    private final AtomicInteger disconnects = new AtomicInteger();
    private final AtomicInteger failuresBeforeSuccess = new AtomicInteger();
    private final CountDownLatch connected = new CountDownLatch(1);

    @Override
    public void publish(String topic, byte[] payload) {}

    @Override
    public void connect() throws Exception {
      connectAttempts.incrementAndGet();
      if (failuresBeforeSuccess.getAndDecrement() > 0) {
        throw new Exception("broker unavailable");
      }

      connected.countDown();
    }

    @Override
    public void disconnect() {
      disconnects.incrementAndGet();
    }
  }
}
