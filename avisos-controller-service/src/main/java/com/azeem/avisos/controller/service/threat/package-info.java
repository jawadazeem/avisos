/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Threat evaluation for vision AI detections. Compares labels returned by the vision service
 * against configured keyword lists to assign alarm severity.
 *
 * <p>{@link com.azeem.avisos.controller.service.threat.KeywordThreatDetector} performs
 * case-insensitive substring matching against two tiers of keywords loaded from {@code
 * problematic-labels.yml} at startup. Critical keywords take precedence over warning keywords. If
 * no keyword matches, severity is NONE and no alarm is raised.
 */
package com.azeem.avisos.controller.service.threat;
