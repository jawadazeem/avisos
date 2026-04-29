/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.service.alarm;

import com.azeem.avisos.alarm.Alarm;
import com.azeem.avisos.infrastructure.logger.Logger;
import com.azeem.avisos.infrastructure.repository.AlarmLogRepository;

import java.util.List;

public class AlarmService {
    AlarmLogRepository alarmLogRepository;
    Logger logger;

    public AlarmService(AlarmLogRepository alarmLogRepository, Logger logger) {
        this.alarmLogRepository = alarmLogRepository;
        this.logger = logger;
    }

    public void save(Alarm alarm) {
        alarmLogRepository.save(alarm);
    }

    public List<Alarm> loadAll() {
        return alarmLogRepository.loadAll();
    }

    public List<Alarm> loadAllActiveAlarms() {
        return alarmLogRepository.loadAllActiveAlarms();
    }
}
