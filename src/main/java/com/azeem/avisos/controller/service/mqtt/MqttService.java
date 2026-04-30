/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.mqtt;

import com.azeem.avisos.common.model.TelemetryPacket;
import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import com.azeem.avisos.controller.model.alarm.AlarmStatus;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.device.DeviceService;
import com.azeem.avisos.controller.service.rekognition.VisionService;
import com.azeem.avisos.controller.service.threat.ThreatDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MqttService {
    private static final Logger log = LoggerFactory.getLogger(MqttService.class);
    private final DeviceService deviceService;
    private final AlarmService alarmService;
    private final ThreatDetector threatDetector;
    private final VisionService visionService;
    private MqttClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String CONTROLLER_CLIENT_ID = "avisos-controller-primary";

    public MqttService(DeviceService deviceService,
                       AlarmService alarmService,
                       VisionService visionService,
                       ThreatDetector threatDetector
    ) {
        this.deviceService = deviceService;
        this.alarmService = alarmService;
        this.visionService = visionService;
        this.threatDetector = threatDetector;
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
        TelemetryPacket packet = null;

        try {
            packet = mapper.readValue(message.getPayload(), TelemetryPacket.class);
        } catch (IOException e) {
            log.error("Discarding malformed packet from topic {}: {}", topic, e.getMessage());
        }

        if (packet == null) {
            log.warn("Received corrupted telemetry packet. Discarding.");
            return;
        }

        deviceService.registerHeartbeat(packet.deviceId());

        if (packet.payload() != null) {
            TelemetryPacket finalPacket = packet;
            Thread.ofVirtual().start(() -> {
                List<String> labels = visionService.detectLabels(finalPacket.payload());
                AlarmSeverity severity = threatDetector.evaluate(labels);
                if (severity != AlarmSeverity.NONE) {
                    alarmService.save(new AlarmRecord(
                        UUID.randomUUID(),
                        finalPacket.deviceId(),
                        severity,
                        labels.toString(),
                        AlarmStatus.ACTIVE,
                        LocalDateTime.now(),
                        null
                    ));
                }
            });
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
