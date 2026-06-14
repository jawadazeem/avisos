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
import com.azeem.avisos.controller.service.storage.ImageStorageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AlarmControllerTest {

  @Mock AlarmService alarmService;
  @Mock ImageStorageService imageStorageService;

  @InjectMocks AlarmController alarmController;

  @Test
  void getActiveAlarms_shouldDelegateToService() {
    AlarmRecord alarm =
        new AlarmRecord(
            UUID.randomUUID(),
            UUID.randomUUID(),
            AlarmSeverity.CRITICAL,
            "threat",
            AlarmStatus.ACTIVE,
            LocalDateTime.now(),
            null,
            null);
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
    verify(alarmService).resolveAlarm(alarmId);
  }

  @Test
  void resolveAlarm_shouldNotLoadOrMutateAlarmBeforeResolving() {
    UUID alarmId = UUID.randomUUID();

    alarmController.resolveAlarm(alarmId);

    verify(alarmService).resolveAlarm(alarmId);
    verifyNoMoreInteractions(alarmService);
  }

  @Test
  void getAlarmImage_shouldReturnStoredImageForAlarm() {
    UUID alarmId = UUID.randomUUID();
    AlarmRecord alarm =
        new AlarmRecord(
            alarmId,
            UUID.randomUUID(),
            AlarmSeverity.CRITICAL,
            "unauthorized access",
            AlarmStatus.ACTIVE,
            LocalDateTime.now(),
            null,
            "camera/flagged.jpg");
    byte[] imageBytes = new byte[] {1, 2, 3};
    when(alarmService.loadAlarm(alarmId)).thenReturn(Optional.of(alarm));
    when(imageStorageService.load("camera/flagged.jpg"))
        .thenReturn(new ImageStorageService.StoredImage(imageBytes, "image/jpeg"));

    ResponseEntity<byte[]> response = alarmController.getAlarmImage(alarmId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertArrayEquals(imageBytes, response.getBody());
    assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
    verify(alarmService).loadAlarm(alarmId);
    verify(imageStorageService).load("camera/flagged.jpg");
  }

  @Test
  void getAlarmImage_shouldReturnNotFoundWhenAlarmHasNoImage() {
    UUID alarmId = UUID.randomUUID();
    AlarmRecord alarm =
        new AlarmRecord(
            alarmId,
            UUID.randomUUID(),
            AlarmSeverity.WARNING,
            "battery low",
            AlarmStatus.ACTIVE,
            LocalDateTime.now(),
            null,
            null);
    when(alarmService.loadAlarm(alarmId)).thenReturn(Optional.of(alarm));

    ResponseEntity<byte[]> response = alarmController.getAlarmImage(alarmId);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    verify(alarmService).loadAlarm(alarmId);
    verifyNoInteractions(imageStorageService);
  }
}
