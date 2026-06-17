/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.broadcast;

import com.azeem.avisos.controller.web.event.AlarmAnalysisCreatedEvent;
import com.azeem.avisos.controller.web.event.AlarmCreatedEvent;
import com.azeem.avisos.controller.web.event.NodeHeartbeatEvent;
import com.azeem.avisos.controller.web.event.VisionAnalysisEvent;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/** Listens for domain events and broadcasts them to STOMP WebSocket topics for the dashboard. */
@Component
public class DashboardEventBroadcaster {
  private final SimpMessagingTemplate messagingTemplate;

  public DashboardEventBroadcaster(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @EventListener
  public void onAlarmCreated(AlarmCreatedEvent event) {
    messagingTemplate.convertAndSend("/topic/alarms", event.getAlarm());
  }

  @EventListener
  public void onNodeHeartbeat(NodeHeartbeatEvent event) {
    messagingTemplate.convertAndSend("/topic/nodes", event.getNode());
  }

  @EventListener
  public void onVisionAnalysis(VisionAnalysisEvent event) {
    messagingTemplate.convertAndSend(
        "/topic/vision", Map.of("nodeId", event.getNodeId(), "response", event.getResponse()));
  }

  @EventListener
  public void onAlarmAnalysis(AlarmAnalysisCreatedEvent event) {
    messagingTemplate.convertAndSend("/topic/alarm", event.getAnalysisRecord());
  }
  // TODO: Fix API endpoint
}
