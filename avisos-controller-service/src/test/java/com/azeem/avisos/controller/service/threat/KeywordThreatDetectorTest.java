/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.threat;

import com.azeem.avisos.controller.model.alarm.AlarmSeverity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeywordThreatDetectorTest {

    @Test
    void shouldReturnCriticalWhenCriticalLabelDetected() {
        KeywordThreatDetector detector = new KeywordThreatDetector(
                List.of("fire", "explosion"),
                List.of("smoke")
        );

        AlarmSeverity result = detector.evaluate(
                List.of("FIRE", "normal activity")
        );

        assertEquals(AlarmSeverity.CRITICAL, result);
    }

    @Test
    void shouldReturnWarningWhenWarningLabelDetected() {
        KeywordThreatDetector detector = new KeywordThreatDetector(
                List.of("fire"),
                List.of("smoke", "heat")
        );

        AlarmSeverity result = detector.evaluate(
                List.of("device", "SMOKE detected")
        );

        assertEquals(AlarmSeverity.WARNING, result);
    }

    @Test
    void shouldReturnNoneWhenNoLabelsMatch() {
        KeywordThreatDetector detector = new KeywordThreatDetector(
                List.of("fire"),
                List.of("smoke")
        );

        AlarmSeverity result = detector.evaluate(
                List.of("network", "battery", "idle")
        );

        assertEquals(AlarmSeverity.NONE, result);
    }

    @Test
    void criticalShouldOverrideWarning() {
        KeywordThreatDetector detector = new KeywordThreatDetector(
                List.of("fire"),
                List.of("fire", "smoke")
        );

        AlarmSeverity result = detector.evaluate(
                List.of("FIRE")
        );

        assertEquals(AlarmSeverity.CRITICAL, result);
    }
}
