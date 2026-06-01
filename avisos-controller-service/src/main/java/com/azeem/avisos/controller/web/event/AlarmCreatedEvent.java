/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.event;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import org.springframework.context.ApplicationEvent;

/** Published when a new alarm is triggered by the telemetry pipeline. */
public class AlarmCreatedEvent extends ApplicationEvent {
  private final AlarmRecord alarm;

  public AlarmCreatedEvent(Object source, AlarmRecord alarm) {
    super(source);
    this.alarm = alarm;
  }

  public AlarmRecord getAlarm() {
    return alarm;
  }
}
