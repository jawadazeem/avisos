/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.azeem.avisos.node.config.MqttConfig;
import com.azeem.avisos.node.config.NodeConfig;
import com.azeem.avisos.node.hardware.BatteryProvider;
import com.azeem.avisos.node.network.api.MqttProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifies telemetry packet construction and MQTT dispatch in the heartbeat pipeline. */
@ExtendWith(MockitoExtension.class)
class HeartbeatServiceTest {

  @Mock MqttProvider mqttProvider;
  @Mock BatteryProvider batteryProvider;
  @Captor ArgumentCaptor<byte[]> payloadCaptor;

  private HeartbeatService heartbeatService;
  private MqttConfig mqttConfig;
  private NodeConfig nodeConfig;

  @BeforeEach
  void setUp() {
    mqttConfig = new MqttConfig("tcp://localhost:1883", "avisos/telemetry");
    nodeConfig = new NodeConfig(UUID.randomUUID(), "test-node", "SENSOR");
    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    heartbeatService =
        new HeartbeatService(mqttProvider, batteryProvider, mqttConfig, nodeConfig, objectMapper);
  }

  @Test
  void sendTelemetry_shouldPublishToConfiguredTopic() {
    when(batteryProvider.getBatteryLevel()).thenReturn(85);

    heartbeatService.sendTelemetry();

    verify(mqttProvider).publish(eq("avisos/telemetry"), any(byte[].class));
  }

  @Test
  void sendTelemetry_shouldIncludeNodeIdInPayload() {
    when(batteryProvider.getBatteryLevel()).thenReturn(72);

    heartbeatService.sendTelemetry();

    verify(mqttProvider).publish(eq(mqttConfig.topic()), payloadCaptor.capture());
    String json = new String(payloadCaptor.getValue());
    assertTrue(json.contains(nodeConfig.nodeId().toString()));
  }

  @Test
  void sendTelemetry_shouldIncludeBatteryLevel() {
    when(batteryProvider.getBatteryLevel()).thenReturn(42);

    heartbeatService.sendTelemetry();

    verify(mqttProvider).publish(eq(mqttConfig.topic()), payloadCaptor.capture());
    String json = new String(payloadCaptor.getValue());
    assertTrue(json.contains("42"));
  }

  @Test
  void sendTelemetry_shouldIncludeNodeName() {
    when(batteryProvider.getBatteryLevel()).thenReturn(100);

    heartbeatService.sendTelemetry();

    verify(mqttProvider).publish(eq(mqttConfig.topic()), payloadCaptor.capture());
    String json = new String(payloadCaptor.getValue());
    assertTrue(json.contains("test-node"));
  }

  @Test
  void sendTelemetry_shouldIncludeHeartbeatPacketType() {
    when(batteryProvider.getBatteryLevel()).thenReturn(50);

    heartbeatService.sendTelemetry();

    verify(mqttProvider).publish(eq(mqttConfig.topic()), payloadCaptor.capture());
    String json = new String(payloadCaptor.getValue());
    assertTrue(json.contains("HEARTBEAT"));
  }

  @Test
  void sendTelemetry_shouldNotThrowOnPublishFailure() {
    when(batteryProvider.getBatteryLevel()).thenReturn(60);
    doThrow(new RuntimeException("Connection lost"))
        .when(mqttProvider)
        .publish(anyString(), any(byte[].class));

    assertDoesNotThrow(() -> heartbeatService.sendTelemetry());
  }
}
