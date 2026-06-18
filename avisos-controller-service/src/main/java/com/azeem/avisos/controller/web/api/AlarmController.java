/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import com.azeem.avisos.controller.service.storage.ImageStorageService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

/** REST API for listing active alarms and resolving them by ID. */
@RestController
@RequestMapping("/api/alarms")
public class AlarmController {
  private final AlarmService alarmService;
  private final ImageStorageService imageStorageService;

  public AlarmController(AlarmService alarmService, ImageStorageService imageStorageService) {
    this.alarmService = alarmService;
    this.imageStorageService = imageStorageService;
  }

  @GetMapping
  public List<AlarmRecord> getActiveAlarms() {
    return alarmService.loadAllActiveAlarms();
  }

  @PostMapping("/{id}/resolve")
  public ResponseEntity<Void> resolveAlarm(@PathVariable("id") UUID id) {
    alarmService.resolveAlarm(id);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}/image")
  public ResponseEntity<byte[]> getAlarmImage(@PathVariable("id") UUID id) {
    return alarmService
        .loadAlarm(id)
        .filter(alarm -> alarm.s3ImageKey() != null && !alarm.s3ImageKey().isBlank())
        .map(this::loadAlarmImage)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  private ResponseEntity<byte[]> loadAlarmImage(AlarmRecord alarm) {
    try {
      ImageStorageService.StoredImage image = imageStorageService.load(alarm.s3ImageKey());
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
          .cacheControl(CacheControl.noStore())
          .contentType(MediaType.parseMediaType(image.contentType()))
          .body(image.bytes());
    } catch (NoSuchBucketException | NoSuchKeyException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
