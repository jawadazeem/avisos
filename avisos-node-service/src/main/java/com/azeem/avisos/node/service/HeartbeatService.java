/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.service;

import com.azeem.avisos.node.common.telemetry.PacketTypeDto;
import com.azeem.avisos.node.common.telemetry.TelemetryPacketDto;
import com.azeem.avisos.node.config.MqttConfig;
import com.azeem.avisos.node.config.NodeConfig;
import com.azeem.avisos.node.hardware.HardwareProviderException;
import com.azeem.avisos.node.hardware.HardwareSnapshot;
import com.azeem.avisos.node.hardware.HardwareTelemetryProvider;
import com.azeem.avisos.node.network.api.MqttProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Core business logic for the Avisos Edge Node. Manages sensor polling and telemetry dispatch. */
public class HeartbeatService {
  private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);
  private static final int SAFE_FALLBACK_BATTERY_LEVEL = 100;

  private final MqttProvider mqttProvider;
  private final HardwareTelemetryProvider hardwareTelemetryProvider;
  private final MqttConfig mqttConfig;
  private final NodeConfig nodeConfig;
  private final ObjectMapper objectMapper;
  private final AtomicInteger lastKnownBatteryLevel =
      new AtomicInteger(SAFE_FALLBACK_BATTERY_LEVEL);

  public HeartbeatService(
      MqttProvider mqttProvider,
      HardwareTelemetryProvider hardwareTelemetryProvider,
      MqttConfig mqttConfig,
      NodeConfig nodeConfig,
      ObjectMapper objectMapper) {
    this.mqttProvider = mqttProvider;
    this.hardwareTelemetryProvider = hardwareTelemetryProvider;
    this.mqttConfig = mqttConfig;
    this.nodeConfig = nodeConfig;
    this.objectMapper = objectMapper;
  }

  public void sendTelemetry() {
    try {
      byte[] frameData = hardwareTelemetryProvider.readFrame();

      HardwareSnapshot snapshot = readHardwareSnapshot();
      TelemetryPacketDto packet = createPacket(frameData, snapshot.batteryPercent());

      byte[] jsonBytes = objectMapper.writeValueAsBytes(packet);
      mqttProvider.publish(mqttConfig.topic(), jsonBytes);

      log.info(
          "Telemetry dispatched: ID={}, Type={}, batteryLevel={}%",
          nodeConfig.nodeId(), packet.type(), packet.batteryLevel());

    } catch (Exception e) {
      log.error("CRITICAL: Telemetry pipeline failure: {}", e.getMessage());
    }
  }

  private HardwareSnapshot readHardwareSnapshot() {
    try {
      HardwareSnapshot snapshot = hardwareTelemetryProvider.readSnapshot();
      lastKnownBatteryLevel.set(snapshot.batteryPercent());
      log.debug("Hardware snapshot read: {}", snapshot);
      return snapshot;
    } catch (HardwareProviderException e) {
      int fallback = lastKnownBatteryLevel.get();
      log.warn(
          "Hardware telemetry unavailable: {}. Using fallback batteryLevel={}%",
          e.getMessage(), fallback);
      return HardwareSnapshot.localBattery(fallback);
    }
  }

  private TelemetryPacketDto createPacket(byte[] frameData, int batteryLevel) {
    PacketTypeDto packetType =
        frameData != null && frameData.length > 0
            ? PacketTypeDto.NETWORK_SCAN
            : PacketTypeDto.HEARTBEAT;

    return new TelemetryPacketDto(
        nodeConfig.nodeId(), batteryLevel, nodeConfig.name(), packetType, frameData, Instant.now());
  }
}
