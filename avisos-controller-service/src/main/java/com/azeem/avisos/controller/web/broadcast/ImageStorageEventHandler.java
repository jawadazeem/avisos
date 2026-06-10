/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.broadcast;

import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.storage.ImageStorageService;
import com.azeem.avisos.controller.web.event.ImageFlaggedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listens for flagged image events and persists the camera frame to S3. Runs asynchronously so S3
 * latency never blocks the alarm pipeline.
 */
@Component
public class ImageStorageEventHandler {
  private static final Logger log = LoggerFactory.getLogger(ImageStorageEventHandler.class);

  private final ImageStorageService imageStorageService;
  private final AlarmService alarmService;

  public ImageStorageEventHandler(
      ImageStorageService imageStorageService, AlarmService alarmService) {
    this.imageStorageService = imageStorageService;
    this.alarmService = alarmService;
  }

  @Async
  @EventListener
  public void onImageFlagged(ImageFlaggedEvent event) {
    try {
      String s3Key =
          imageStorageService.store(
              event.getMqttSource(), event.getNodeId(), event.getImageData(), event.getTimestamp());

      alarmService.attachImage(event.getAlarmId(), s3Key);
    } catch (Exception e) {
      log.error(
          "Failed to store flagged image for alarm {} — alarm is saved but image is lost: {}",
          event.getAlarmId(),
          e.getMessage());
    }
  }
}
