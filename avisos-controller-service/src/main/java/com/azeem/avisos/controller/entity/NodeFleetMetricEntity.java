/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.entity;

import java.time.Instant;

@Entity
public class NodeFleetMetricEntity {

    private Instant timestamp;

    private long totalNodesEvaluated;
    private double responsiveRatio;
    private double batteryAbove50Ratio;
    private double avgSecondsSinceLastSeen;
}
