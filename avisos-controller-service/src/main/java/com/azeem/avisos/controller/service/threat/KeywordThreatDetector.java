/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.threat;

import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import java.util.List;

public record KeywordThreatDetector(List<String> criticalLabels, List<String> warningLabels)
    implements ThreatDetector {

  @Override
  public AlarmSeverity evaluate(List<String> detectedLabels) {

    List<String> lowerLabels = detectedLabels.stream().map(String::toLowerCase).toList();

    boolean isCritical =
        lowerLabels.stream().anyMatch(label -> criticalLabels.stream().anyMatch(label::contains));

    if (isCritical) {
      return AlarmSeverity.CRITICAL;
    }

    boolean isWarning =
        lowerLabels.stream().anyMatch(label -> warningLabels.stream().anyMatch(label::contains));

    if (isWarning) {
      return AlarmSeverity.WARNING;
    }

    return AlarmSeverity.NONE;
  }
}
