/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.node;

import com.azeem.avisos.controller.model.node.FleetMetricRecord;
import com.azeem.avisos.controller.repository.NodeFleetMetricRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FleetMetricService {

    private final NodeFleetMetricRepository repository;

    public FleetMetricService(NodeFleetMetricRepository repository) {
        this.repository = repository;
    }

    public void saveMetric(FleetMetricRecord metric) {
        repository.saveMetric(metric);
    }

    public List<FleetMetricRecord> getMetrics() {
        return repository.getMetrics();
    }

    public List<FleetMetricRecord> getMetrics(int limit) {
        return repository.getMetrics(limit);
    }

    public Optional<FleetMetricRecord> getLatestMetric() {
        return repository.getLatestMetric();
    }
}
