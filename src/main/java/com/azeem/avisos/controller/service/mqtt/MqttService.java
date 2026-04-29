/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.mqtt;

import com.azeem.avisos.common.model.TelemetryPacket;
import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.repository.TelemetryRepository;
import com.azeem.avisos.controller.service.device.DeviceService;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.device.DeviceServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class MqttService {
    private static final Logger log = LoggerFactory.getLogger(MqttService.class);
    private final DeviceService deviceService;
    private final AlarmService alarmService;
    private MqttClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String CONTROLLER_CLIENT_ID = "avisos-controller-primary";

    public MqttService(DeviceService deviceService, AlarmService alarmService) {
        this.deviceService = deviceService;
        this.alarmService = alarmService;
    }

    @PostConstruct
    public void init() {
        String broker = "tcp://localhost:1883";

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(false);
        options.setConnectionTimeout(10);

        try {
            client = new MqttClient(broker, CONTROLLER_CLIENT_ID);
            client.connect(options);

            log.info("Avisos Hub successfully peered with MQTT Broker at {}", broker);
            start();

        } catch (MqttException e) {
            log.error("Critical: MQTT Peering failed. Ingress is offline.", e);
        }
    }

    public void start() {
        try {
            client.subscribe("avisos/telemetry/#", (topic, message) -> {
                Thread.ofVirtual().start(() -> handleMessage(topic, message));
            });
        } catch (MqttException e) {
            log.warn("MQTT subscription failed for topic avisos/telemetry/#", e);
            throw new IllegalStateException("Cannot start MQTT subscription", e);
        }
    }

    private void handleMessage(String topic, MqttMessage message) {
        try {
            byte[] payload = message.getPayload();

            // Use the topic structure to route logic
            if (topic.contains("/heartbeat")) {
                TelemetryPacket packet = mapper.readValue(payload, TelemetryPacket.class);
                deviceService.registerHeartbeat(packet.deviceId());
            }
            else if (topic.contains("/trigger")) {
                AlarmRecord alarm = mapper.readValue(payload, AlarmRecord.class);
                alarmService.save(alarm);
                log.info("ALARM TRIGGERED: Node {} reported {}", alarm.deviceUuid(), alarm.reason());
            }
        } catch (IOException e) {
            log.error("Discarding malformed packet from topic {}: {}", topic, e.getMessage());
        }
    }

    private UUID extractUuid(MqttMessage message) {
        try {
            return mapper.readValue(message.getPayload(), TelemetryPacket.class).deviceId();
        } catch (IOException e) {
            log.error("Could not parse heartbeat message: ", e);
            throw new RuntimeException(e);
        }
    }
    private AlarmRecord mapToAlarm(MqttMessage message) {
        try {
            return mapper.readValue(message.getPayload(), AlarmRecord.class);
        } catch (IOException e) {
            log.error("Could not parse alarm trigger message: ", e);
            throw new RuntimeException(e);
        }
    }
}
