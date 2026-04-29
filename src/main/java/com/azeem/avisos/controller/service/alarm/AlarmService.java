/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.alarm;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.repository.AlarmRepository;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AlarmService {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(AlarmService.class);
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
        log.info("Alarm ID {} has been marked as RESOLVED", alarmId);
    }
}
