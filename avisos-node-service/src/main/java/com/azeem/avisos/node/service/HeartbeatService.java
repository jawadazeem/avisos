/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.service;

import com.azeem.avisos.node.config.MqttConfig;
import com.azeem.avisos.node.config.NodeConfig;
import com.azeem.avisos.node.hardware.BatteryProvider;
import com.azeem.avisos.node.network.api.MqttProvider;
import com.azeem.avisos.node.common.telemetry.TelemetryPacketDto;
import com.azeem.avisos.node.common.telemetry.PacketTypeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Random;

/**
 * Core business logic for the Avisos Edge Node.
 * Manages sensor polling and telemetry dispatch.
 */
public class HeartbeatService {
    private static final Logger log = LoggerFactory.getLogger(HeartbeatService.class);

    private final MqttProvider mqttProvider;
    private final BatteryProvider batteryProvider;
    private final MqttConfig mqttConfig;
    private final NodeConfig nodeConfig;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    public HeartbeatService(MqttProvider mqttProvider,
                            BatteryProvider batteryProvider,
                            MqttConfig mqttConfig,
                            NodeConfig nodeConfig,
                            ObjectMapper objectMapper) {
        this.mqttProvider = mqttProvider;
        this.batteryProvider = batteryProvider;
        this.mqttConfig = mqttConfig;
        this.nodeConfig = nodeConfig;
        this.objectMapper = objectMapper;
    }

    public void sendTelemetry() {
        try {
            byte[] frameData = new byte[1024];
            random.nextBytes(frameData);

            TelemetryPacketDto packet = createPacket(frameData);

            byte[] jsonBytes = objectMapper.writeValueAsBytes(packet);
            mqttProvider.publish(mqttConfig.topic(), jsonBytes);

            log.info("Telemetry dispatched: ID={}, Type={}", nodeConfig.nodeId(), packet.type());

        } catch (Exception e) {
            log.error("CRITICAL: Telemetry pipeline failure: {}", e.getMessage());
        }
    }

    private TelemetryPacketDto createPacket(byte[] frameData) {
        return new TelemetryPacketDto(
                nodeConfig.nodeId(),
                batteryProvider.getBatteryLevel(),
                nodeConfig.name(),
                PacketTypeDto.HEARTBEAT,
                frameData,
                Instant.now()
        );
    }
}
