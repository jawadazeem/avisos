/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.threat;

import com.azeem.avisos.controller.model.alarm.AlarmSeverity;

import java.util.List;

public interface ThreatDetector {
    AlarmSeverity evaluate(List<String> detectedLabels);
}
