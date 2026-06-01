/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.event;

import com.azeem.avisos.controller.model.node.NodeRecord;
import org.springframework.context.ApplicationEvent;

public class NodeHeartbeatEvent extends ApplicationEvent {
  private final NodeRecord node;

  public NodeHeartbeatEvent(Object source, NodeRecord node) {
    super(source);
    this.node = node;
  }

  public NodeRecord getNode() {
    return node;
  }
}
