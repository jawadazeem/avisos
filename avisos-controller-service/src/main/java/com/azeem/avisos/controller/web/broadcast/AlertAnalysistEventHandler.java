/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.broadcast;

import com.azeem.avisos.controller.model.alarm.AlarmAnalysisRecord;
import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.service.ai.rag.AlarmAnalystService;
import com.azeem.avisos.controller.web.event.AlarmCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;

public class AlertAnalysistEventHandler {
    private final AlarmAnalystService alarmAnalystService;

    public AlertAnalysistEventHandler(AlarmAnalystService alarmAnalystService) {
        this.alarmAnalystService = alarmAnalystService;
    }

    @Async
    @EventListener
    public void onAlarmCreated(AlarmCreatedEvent event) {
        AlarmRecord alarmRecord = event.getAlarm();
        String summary = alarmAnalystService.generateAlert(alarmRecord.deviceUuid().toString(),
            alarmRecord.toString(),
            alarmRecord.reason());

        alarmAnalystService.saveAnalysis(new AlarmAnalysisRecord(
            alarmRecord.id().toString(),
            summary,
            "IRA1",
            LocalDateTime.now()));
    }
}
