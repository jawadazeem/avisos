/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import com.azeem.avisos.controller.model.alarm.AlarmStatus;
import com.azeem.avisos.controller.service.alarm.AlarmService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AlarmControllerTest {

  @Mock AlarmService alarmService;

  @InjectMocks AlarmController alarmController;

  @Captor ArgumentCaptor<AlarmRecord> alarmCaptor;

  @Test
  void getActiveAlarms_shouldDelegateToService() {
    AlarmRecord alarm =
        new AlarmRecord(UUID.randomUUID(), UUID.randomUUID(), AlarmSeverity.CRITICAL, "threat",
            AlarmStatus.ACTIVE, LocalDateTime.now(), null);
    when(alarmService.loadAllActiveAlarms()).thenReturn(List.of(alarm));

    List<AlarmRecord> result = alarmController.getActiveAlarms();

    assertEquals(1, result.size());
    assertEquals(alarm, result.get(0));
    verify(alarmService).loadAllActiveAlarms();
  }

  @Test
  void getActiveAlarms_shouldReturnEmptyListWhenNoAlarms() {
    when(alarmService.loadAllActiveAlarms()).thenReturn(List.of());

    List<AlarmRecord> result = alarmController.getActiveAlarms();

    assertTrue(result.isEmpty());
  }

  @Test
  void resolveAlarm_shouldCallServiceWithCorrectId() {
    UUID alarmId = UUID.randomUUID();

    ResponseEntity<Void> response = alarmController.resolveAlarm(alarmId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(alarmService).resolveAlarm(alarmCaptor.capture());
    assertEquals(alarmId, alarmCaptor.getValue().id());
  }

  @Test
  void resolveAlarm_shouldPassStubWithNullFieldsExceptId() {
    UUID alarmId = UUID.randomUUID();

    alarmController.resolveAlarm(alarmId);

    verify(alarmService).resolveAlarm(alarmCaptor.capture());
    AlarmRecord stub = alarmCaptor.getValue();
    assertEquals(alarmId, stub.id());
    assertNull(stub.deviceUuid());
    assertNull(stub.reason());
    assertEquals(AlarmSeverity.NONE, stub.severity());
    assertEquals(AlarmStatus.ACTIVE, stub.status());
  }
}
