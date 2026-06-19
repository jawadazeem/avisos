/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import com.azeem.avisos.controller.model.node.FleetMetricRecord;
import com.azeem.avisos.controller.service.node.FleetMetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for querying historical fleet-wide telemetry metrics.
 */
@RestController
@RequestMapping("/api/fleet-metrics")
public class FleetMetricController {

    private final FleetMetricService fleetMetricService;

    public FleetMetricController(FleetMetricService fleetMetricService) {
        this.fleetMetricService = fleetMetricService;
    }

    @GetMapping
    public List<FleetMetricRecord> getFleetMetrics() {
        return fleetMetricService.getMetrics();
    }

    @GetMapping("/latest")
    public ResponseEntity<FleetMetricRecord> getLatestMetric() {
        return fleetMetricService
            .getLatestMetric()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
