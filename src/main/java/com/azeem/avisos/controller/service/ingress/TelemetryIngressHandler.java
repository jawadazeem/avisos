/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.ingress;

import com.azeem.avisos.common.model.TelemetryPacket;
import com.azeem.avisos.controller.infrastructure.vision.VisionClient;
import com.azeem.avisos.controller.model.ingress.data.IngressMessage;
import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import com.azeem.avisos.controller.model.alarm.AlarmStatus;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.device.DeviceService;
import com.azeem.avisos.controller.service.threat.ThreatDetector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TelemetryIngressHandler implements IngressDataHandler<IngressMessage> {
    private static final Logger log = LoggerFactory.getLogger(TelemetryIngressHandler.class);
    private final DeviceService deviceService;
    private final AlarmService alarmService;
    private final ThreatDetector threatDetector;
    private final VisionClient visionClient;
    private final ObjectMapper mapper;

    public TelemetryIngressHandler(DeviceService deviceService,
                                  AlarmService alarmService,
                                  VisionClient visionClient,
                                  ThreatDetector threatDetector,
                                  ObjectMapper mapper
    ) {
        this.deviceService = deviceService;
        this.alarmService = alarmService;
        this.visionClient = visionClient;
        this.threatDetector = threatDetector;
        this.mapper = mapper;
    }

    // TODO: Form a VisionRequest object instead of passing raw payload to vision client.
    //  Accept a VisionResponse, then decide to create the Alarm.
    @Override
    public void handle(IngressMessage message) {
        TelemetryPacket packet = null;

        try {
            packet = mapper.readValue(message.payload(), TelemetryPacket.class);
        } catch (IOException e) {
            log.error("Discarding malformed packet from source {} (payloadLen={}): {}",
                    message.source(), safePayloadLength(message), e.getMessage());
        }

        if (packet == null) {
            log.warn("Received corrupted telemetry packet. Discarding.");
            return;
        }

        deviceService.registerHeartbeat(packet.deviceId());

        if (packet.payload() != null) {
            try {
                List<String> labels = visionClient.detectLabels(packet.payload());
                AlarmSeverity severity = threatDetector.evaluate(labels);

                String formattedLabels = formatLabelsForAlarm(labels);
                if (severity != AlarmSeverity.NONE) {
                    alarmService.save(
                            new AlarmRecord(
                                    UUID.randomUUID(),
                                    packet.deviceId(),
                                    severity,
                                    formattedLabels,
                                    AlarmStatus.ACTIVE,
                                    LocalDateTime.now(),
                                    null
                            )
                    );
                }

                log.info("Alarm saved for device {} severity={} labelsCount={}",
                        packet.deviceId(), severity, labels.size());

            } catch (Exception e) {
                log.error("Error processing telemetry for device {}: {}",
                        packet.deviceId(), e.getMessage(), e);
            }
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
