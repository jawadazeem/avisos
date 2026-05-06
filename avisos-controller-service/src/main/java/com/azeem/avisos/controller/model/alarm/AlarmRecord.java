/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.alarm;

import java.time.LocalDateTime;
import java.util.UUID;

public record AlarmRecord (
    UUID id,
    UUID deviceUuid,
    AlarmSeverity severity,
    String reason,
    AlarmStatus status,
    LocalDateTime triggeredAtTimestamp,
    LocalDateTime resolvedAtTimestamp // Can be null if the alarm is still active
) {}
