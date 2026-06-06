/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.event;

import com.azeem.avisos.controller.model.vision.VisionResponse;
import java.util.UUID;
import org.springframework.context.ApplicationEvent;

/** Published when a vision AI analysis completes for an incoming telemetry frame. */
public class VisionAnalysisEvent extends ApplicationEvent {
  private final VisionResponse response;
  private final UUID nodeId;

  public VisionAnalysisEvent(Object source, VisionResponse response, UUID nodeId) {
    super(source);
    this.response = response;
    this.nodeId = nodeId;
  }

  public VisionResponse getResponse() {
    return response;
  }

  public UUID getNodeId() {
    return nodeId;
  }
}
