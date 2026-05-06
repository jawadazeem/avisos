/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.alarm;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import com.azeem.avisos.controller.model.alarm.AlarmStatus;
import com.azeem.avisos.controller.repository.AlarmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlarmServiceTest {

    @Mock
    AlarmRepository alarmRepository;

    @InjectMocks
    AlarmService alarmService;

    @Captor
    ArgumentCaptor<AlarmRecord> alarmRecordCaptor;

    AlarmRecord sampleAlarm;

    @BeforeEach
    void setUp() {
        sampleAlarm = new AlarmRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                AlarmSeverity.CRITICAL,
                "Test reason",
                AlarmStatus.ACTIVE,
                LocalDateTime.now(),
                null
        );
    }

    @Test
    void save_shouldDelegateToRepository_andLog() {
        // when
        alarmService.save(sampleAlarm);

        // then
        verify(alarmRepository, times(1)).triggerAlarm(alarmRecordCaptor.capture());
        AlarmRecord captured = alarmRecordCaptor.getValue();
        assertEquals(sampleAlarm.id(), captured.id());
        assertEquals(sampleAlarm.deviceUuid(), captured.deviceUuid());
        assertEquals(sampleAlarm.reason(), captured.reason());
        assertEquals(sampleAlarm.status(), captured.status());
    }

    @Test
    void loadAllActiveAlarms_shouldReturnRepositoryResults() {
        // given
        List<AlarmRecord> expected = List.of(sampleAlarm);
        when(alarmRepository.getActiveAlarms()).thenReturn(expected);

        // when
        List<AlarmRecord> actual = alarmService.loadAllActiveAlarms();

        // then
        assertSame(expected, actual, "Should return the exact list provided by repository");
        verify(alarmRepository, times(1)).getActiveAlarms();
    }

    @Test
    void resolveAlarm_shouldCallRepositoryResolve_andLog() {
        // when
        alarmService.resolveAlarm(sampleAlarm);

        // then
        verify(alarmRepository, times(1)).resolveAlarm(sampleAlarm.id());
    }
}

