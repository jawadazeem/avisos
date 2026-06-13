/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.event;

import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEvent;

/**
 * Published when the vision pipeline flags an image that triggered an alarm. Carries the raw image
 * bytes so an event listener can persist them to S3 without blocking the alarm pipeline.
 */
public class ImageFlaggedEvent extends ApplicationEvent {
  private final UUID alarmId;
  private final UUID nodeId;
  private final String source;
  private final byte[] imageData;
  private final Instant timestamp;

  public ImageFlaggedEvent(
      Object source,
      UUID alarmId,
      UUID nodeId,
      String mqttSource,
      byte[] imageData,
      Instant timestamp) {
    super(source);
    this.alarmId = alarmId;
    this.nodeId = nodeId;
    this.source = mqttSource;
    this.imageData = imageData;
    this.timestamp = timestamp;
  }

  public UUID getAlarmId() {
    return alarmId;
  }

  public UUID getNodeId() {
    return nodeId;
  }

  public String getMqttSource() {
    return source;
  }

  public byte[] getImageData() {
    return imageData;
  }

  public Instant getCapturedAt() {
    return timestamp;
  }
}
