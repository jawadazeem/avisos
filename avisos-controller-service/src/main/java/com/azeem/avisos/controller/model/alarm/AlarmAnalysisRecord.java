/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.alarm;

import java.time.LocalDateTime;

public record AlarmAnalysisRecord(
    String alarmId, String analysisText, String promptVersion, LocalDateTime createdAt) {}
