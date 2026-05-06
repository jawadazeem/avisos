/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.threat;

import com.azeem.avisos.controller.model.alarm.AlarmSeverity;

import java.util.List;

public record KeywordThreatDetector(
        List<String> criticalLabels,
        List<String> warningLabels
) implements ThreatDetector {

    @Override
    public AlarmSeverity evaluate(List<String> detectedLabels) {
        List<String> lowerLabels = detectedLabels.stream()
                .map(String::toLowerCase)
                .toList();

        if (lowerLabels.stream().anyMatch(criticalLabels::contains)) {
            return AlarmSeverity.CRITICAL;
        }

        if (lowerLabels.stream().anyMatch(warningLabels::contains)) {
            return AlarmSeverity.WARNING;
        }

        return AlarmSeverity.NONE;
    }
}
