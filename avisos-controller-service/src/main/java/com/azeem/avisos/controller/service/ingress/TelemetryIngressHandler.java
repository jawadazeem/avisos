/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.ingress;

import com.azeem.avisos.controller.common.telemetry.PacketTypeDto;
import com.azeem.avisos.controller.common.telemetry.TelemetryPacketDto;
import com.azeem.avisos.controller.config.VisionConfig;
import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import com.azeem.avisos.controller.model.alarm.AlarmStatus;
import com.azeem.avisos.controller.model.ingress.data.IngressMessage;
import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.model.node.NodeStatus;
import com.azeem.avisos.controller.model.vision.Prediction;
import com.azeem.avisos.controller.model.vision.VisionRequest;
import com.azeem.avisos.controller.model.vision.VisionResponse;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.node.NodeService;
import com.azeem.avisos.controller.service.threat.ThreatDetector;
import com.azeem.avisos.controller.service.vision.VisionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Handles raw MQTT data (IngressMessage) and deserializes it into
 * TelemetryPacketDto for processing by the Vision and Alarm services.
 */
public class TelemetryIngressHandler implements IngressDataHandler<IngressMessage> {
    private static final Logger log = LoggerFactory.getLogger(TelemetryIngressHandler.class);
    private static final String TELEMETRY_NODE_TYPE = "MQTT_TELEMETRY_NODE";
    private final NodeService deviceService;
    private final AlarmService alarmService;
    private final ThreatDetector threatDetector;
    private final VisionService visionService;
    private final VisionConfig visionConfig;
    private final ObjectMapper objectMapper;

    public TelemetryIngressHandler(NodeService deviceService,
                                   AlarmService alarmService,
                                   VisionService visionService,
                                   VisionConfig visionConfig,
                                   ThreatDetector threatDetector,
                                   ObjectMapper objectMapper
    ) {
        this.deviceService = deviceService;
        this.alarmService = alarmService;
        this.visionConfig = visionConfig;
        this.visionService = visionService;
        this.threatDetector = threatDetector;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(IngressMessage message) {
        TelemetryPacketDto packet = null;

        try {
            packet = objectMapper.readValue(message.payload(), TelemetryPacketDto.class);
        } catch (IOException e) {
            log.error("Discarding malformed packet from source {} (payloadLen={}): {}",
                    message.source(), safePayloadLength(message), e.getMessage());
        }

        if (packet == null) {
            log.warn("Received corrupted telemetry packet. Discarding.");
            return;
        }

        deviceService.updateNodeHeartbeat(
                new NodeRecord(
                        packet.nodeId(),
                        packet.nodeName(),
                        TELEMETRY_NODE_TYPE,
                        NodeStatus.RESPONSIVE,
                        packet.batteryLevel(),
                        LocalDateTime.now()
                )
        );
        deviceService.registerHeartbeat(packet.nodeId());
        if (packet.type() == PacketTypeDto.HEARTBEAT) {
            log.info(
                    "Heartbeat registered for device {} batteryLevel={}%",
                    packet.nodeId(),
                    packet.batteryLevel()
            );
            return;
        }

        try {
            VisionResponse visionResponse = visionService.analyze(
                    new VisionRequest(
                            packet.payload(),
                            UUID.randomUUID().toString(),
                            visionConfig.minConfidence(),
                            packet.nodeId().toString()
                    )
            );

            List<String> labels = visionResponse.predictions() != null
                    ? visionResponse.predictions().stream()
                    .map(Prediction::label)
                    .toList()
                    : List.of();

            AlarmSeverity severity = threatDetector.evaluate(labels);

            String formattedLabels = formatLabelsForAlarm(labels);
            if (severity != AlarmSeverity.NONE) {
                alarmService.save(
                        new AlarmRecord(
                                UUID.randomUUID(),
                                packet.nodeId(),
                                severity,
                                formattedLabels,
                                AlarmStatus.ACTIVE,
                                LocalDateTime.now(),
                                null
                        )
                );
            }

            log.info("Alarm saved for device {} severity={} labelsCount={}",
                    packet.nodeId(), severity, labels.size());

        } catch (Exception e) {
            log.error("Error processing telemetry for device {}: {}",
                    packet.nodeId(), e.getMessage(), e);
        }
    }

    private int safePayloadLength(IngressMessage message) {
        byte[] p = message == null ? null : message.payload();
        return p == null ? 0 : p.length;
    }

    private String formatLabelsForAlarm(List<String> labels) {
        String joined = String.join(",", labels);
        int max = 1024;
        return joined.length() <= max ? joined : joined.substring(0, max) + "...";
    }
}
