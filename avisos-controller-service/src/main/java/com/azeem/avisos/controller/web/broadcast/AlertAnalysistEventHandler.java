/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.broadcast;

import com.azeem.avisos.controller.model.alarm.AlarmAnalysisRecord;
import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.service.ai.rag.AlarmAnalystService;
import com.azeem.avisos.controller.web.event.AlarmAnalysisCreatedEvent;
import com.azeem.avisos.controller.web.event.AlarmCreatedEvent;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AlertAnalysistEventHandler {
  private final AlarmAnalystService alarmAnalystService;
  private final ApplicationEventPublisher eventPublisher;

  public AlertAnalysistEventHandler(
      AlarmAnalystService alarmAnalystService, ApplicationEventPublisher eventPublisher) {
    this.alarmAnalystService = alarmAnalystService;
    this.eventPublisher = eventPublisher;
  }

  @Async
  @EventListener
  public void onAlarmCreated(AlarmCreatedEvent event) {
    AlarmRecord alarmRecord = event.getAlarm();
    String summary =
        alarmAnalystService.generateAlert(
            alarmRecord.deviceUuid().toString(), alarmRecord.toString(), alarmRecord.reason());

    AlarmAnalysisRecord record =
        new AlarmAnalysisRecord(alarmRecord.id().toString(), summary, "IRA1", LocalDateTime.now());
    alarmAnalystService.saveAnalysis(record);
    eventPublisher.publishEvent(new AlarmAnalysisCreatedEvent(this, record));
  }
}
