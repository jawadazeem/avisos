/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.alarm;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.repository.AlarmRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Manages alarm persistence and lifecycle (trigger, query, resolve). */
@Service
public class AlarmService {
  private static final Logger log = LoggerFactory.getLogger(AlarmService.class);
  AlarmRepository alarmRepository;

  public AlarmService(AlarmRepository alarmRepository) {
    this.alarmRepository = alarmRepository;
  }

  public void save(AlarmRecord alarm) {
    alarmRepository.triggerAlarm(alarm);
    log.info("New alarm triggered for device: {}", alarm.deviceUuid());
  }

  public List<AlarmRecord> loadAllActiveAlarms() {
    return alarmRepository.getActiveAlarms();
  }

  public void resolveAlarm(AlarmRecord alarm) {
    alarmRepository.resolveAlarm(alarm.id());
    log.info("Alarm ID {} has been marked as RESOLVED", alarm.id());
  }

  public void attachImage(java.util.UUID alarmId, String s3ImageKey) {
    alarmRepository.updateS3ImageKey(alarmId, s3ImageKey);
    log.info("Image attached to alarm {} → {}", alarmId, s3ImageKey);
  }
}
