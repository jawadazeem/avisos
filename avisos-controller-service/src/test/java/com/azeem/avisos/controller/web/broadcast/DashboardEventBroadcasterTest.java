/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.broadcast;

import static org.mockito.Mockito.*;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import com.azeem.avisos.controller.model.alarm.AlarmStatus;
import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.model.node.NodeStatus;
import com.azeem.avisos.controller.model.vision.Prediction;
import com.azeem.avisos.controller.model.vision.VisionResponse;
import com.azeem.avisos.controller.web.event.AlarmCreatedEvent;
import com.azeem.avisos.controller.web.event.NodeHeartbeatEvent;
import com.azeem.avisos.controller.web.event.VisionAnalysisEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class DashboardEventBroadcasterTest {

  @Mock SimpMessagingTemplate messagingTemplate;

  @InjectMocks DashboardEventBroadcaster broadcaster;

  @Test
  void onAlarmCreated_shouldSendAlarmToTopic() {
    AlarmRecord alarm =
        new AlarmRecord(
            UUID.randomUUID(),
            UUID.randomUUID(),
            AlarmSeverity.CRITICAL,
            "knife detected",
            AlarmStatus.ACTIVE,
            LocalDateTime.now(),
            null,
            null);
    AlarmCreatedEvent event = new AlarmCreatedEvent(this, alarm);

    broadcaster.onAlarmCreated(event);

    verify(messagingTemplate).convertAndSend("/topic/alarms", alarm);
  }

  @Test
  void onNodeHeartbeat_shouldSendNodeToTopic() {
    NodeRecord node =
        new NodeRecord(
            UUID.randomUUID(),
            "node-01",
            "MQTT_TELEMETRY_NODE",
            NodeStatus.RESPONSIVE,
            92.5,
            LocalDateTime.now());
    NodeHeartbeatEvent event = new NodeHeartbeatEvent(this, node);

    broadcaster.onNodeHeartbeat(event);

    verify(messagingTemplate).convertAndSend("/topic/nodes", node);
  }

  @Test
  void onVisionAnalysis_shouldSendResponseAndNodeIdToTopic() {
    UUID nodeId = UUID.randomUUID();
    VisionResponse response =
        new VisionResponse(
            true, List.of(new Prediction("person", 0.95, 10, 20, 200, 300)), 120, "GPU");
    VisionAnalysisEvent event = new VisionAnalysisEvent(this, response, nodeId);

    broadcaster.onVisionAnalysis(event);

    verify(messagingTemplate)
        .convertAndSend("/topic/vision", Map.of("nodeId", nodeId, "response", response));
  }
}
