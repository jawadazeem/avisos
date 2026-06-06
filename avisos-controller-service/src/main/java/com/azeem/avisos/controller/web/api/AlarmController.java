/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import com.azeem.avisos.controller.model.alarm.AlarmStatus;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST API for listing active alarms and resolving them by ID. */
@RestController
@RequestMapping("/api/alarms")
public class AlarmController {
  private final AlarmService alarmService;

  public AlarmController(AlarmService alarmService) {
    this.alarmService = alarmService;
  }

  @GetMapping
  public List<AlarmRecord> getActiveAlarms() {
    return alarmService.loadAllActiveAlarms();
  }

  @PostMapping("/{id}/resolve")
  public ResponseEntity<Void> resolveAlarm(@PathVariable UUID id) {
    AlarmRecord stub =
        new AlarmRecord(id, null, AlarmSeverity.NONE, null, AlarmStatus.ACTIVE, null, null);
    alarmService.resolveAlarm(stub);
    return ResponseEntity.ok().build();
  }
}
