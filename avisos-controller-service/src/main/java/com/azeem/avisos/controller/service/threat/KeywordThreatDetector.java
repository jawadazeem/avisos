/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.threat;

import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import java.util.List;
import java.util.Locale;

public record KeywordThreatDetector(List<String> criticalLabels, List<String> warningLabels)
    implements ThreatDetector {

  public KeywordThreatDetector {
    criticalLabels = normalize(criticalLabels);
    warningLabels = normalize(warningLabels);
  }

  @Override
  public AlarmSeverity evaluate(List<String> detectedLabels) {

    List<String> lowerLabels = normalize(detectedLabels);

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

  private static List<String> normalize(List<String> labels) {
    if (labels == null) {
      return List.of();
    }
    return labels.stream()
        .filter(label -> label != null && !label.isBlank())
        .map(label -> label.trim().toLowerCase(Locale.ROOT))
        .toList();
  }
}
