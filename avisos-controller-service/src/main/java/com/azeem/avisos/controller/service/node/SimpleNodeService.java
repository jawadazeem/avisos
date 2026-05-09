/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.node;

import com.azeem.avisos.controller.entity.node.NodeEntity;
import com.azeem.avisos.controller.mapper.node.NodeMapper;
import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.model.node.NodeStatus;
import com.azeem.avisos.controller.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SimpleNodeService implements NodeService {

    private static final Logger log = LoggerFactory.getLogger(SimpleNodeService.class);

    private final NodeRepository nodeRepository;

    // In-memory state
    private final Map<UUID, NodeEntity> activeRegistry = new ConcurrentHashMap<>();
    private final Map<UUID, Long> floodProtector = new ConcurrentHashMap<>();

    // Metrics
    private final AtomicLong acceptedHeartbeats = new AtomicLong();
    private final AtomicLong rejectedHeartbeats = new AtomicLong();
    private final AtomicLong dbFailures = new AtomicLong();

    private static final long MIN_HEARTBEAT_INTERVAL_MS = 2000;

    public SimpleNodeService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void registerHeartbeat(UUID uuid) {
        long now = System.currentTimeMillis();

        if (isFlooding(uuid, now)) {
            rejectedHeartbeats.incrementAndGet();
            return;
        }

        try {
            activeRegistry.compute(uuid, (id, existing) -> {

                if (existing == null) {
                    try {
                        existing = nodeRepository.getNodeEntity(id.toString());
                    } catch (Exception e) {
                        log.error("DB fetch failed for node={}", id, e);
                        dbFailures.incrementAndGet();
                        return null;
                    }
                }

                if (existing == null) {
                    log.warn("Heartbeat from unknown node={}", id);
                    return null;
                }

                NodeEntity updated = existing.withHeartbeat(
                        existing.batteryLevel(),
                        NodeStatus.RESPONSIVE.name()
                );

                safeDbUpdate(id);

                acceptedHeartbeats.incrementAndGet();

                log.debug("Heartbeat accepted node={}", id);

                return updated;
            });

        } catch (Exception e) {
            log.error("Unexpected error processing heartbeat for node={}", uuid, e);
            dbFailures.incrementAndGet();
        }
    }

    @Override
    public void checkStaleNodes() {
        try {
            int threshold = 60;
            int affected = nodeRepository.markStaleNodesOffline(threshold);

            log.info("Stale cleanup: marked {} nodes OFFLINE", affected);

            LocalDateTime cutoff = LocalDateTime.now().minusSeconds(threshold);

            activeRegistry.entrySet().removeIf(entry ->
                    entry.getValue() != null &&
                            entry.getValue().lastSeen().isBefore(cutoff)
            );

            floodProtector.keySet().removeIf(uuid ->
                    !activeRegistry.containsKey(uuid)
            );

        } catch (Exception e) {
            log.error("Failed during stale node cleanup", e);
            dbFailures.incrementAndGet();
        }
    }

    @Override
    public void updateNodeHeartbeat(NodeRecord nodeRecord) {
        try {
            nodeRepository.saveNode(nodeRecord);

            NodeEntity entity =
                    nodeRepository.getNodeEntity(nodeRecord.uuid().toString());

            if (entity != null) {
                activeRegistry.put(nodeRecord.uuid(), entity);
            }

        } catch (Exception e) {
            log.error("Failed updating full node record uuid={}", nodeRecord.uuid(), e);
            dbFailures.incrementAndGet();
        }
    }

    @Override
    public List<UUID> getRegisteredNodes() {
        try {
            return nodeRepository.getRegisteredNodeUuids().stream()
                    .map(UUID::fromString)
                    .toList();
        } catch (Exception e) {
            log.error("Failed fetching registered nodes", e);
            return List.of();
        }
    }

    @Override
    public Optional<NodeRecord> getNode(UUID nodeId) {
        // Tier 1: Check Memory (Active State)
        NodeEntity entity = activeRegistry.get(nodeId);

        if (entity != null) {
            return Optional.of(NodeMapper.toDomain(entity));
        }

        // Tier 2: Check Database (Persistent State)
        try {
            NodeRecord record = nodeRepository.getNode(nodeId);
            if (record != null) {
                // Optional: Hydrate memory registry if we found it in DB
                // activeRegistry.put(nodeId, NodeMapper.toEntity(record));
                return Optional.of(record);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve node {} from repository: {}", nodeId, e.getMessage());
        }

        return Optional.empty();
    }

    private boolean isFlooding(UUID uuid, long now) {
        Long lastSeen = floodProtector.put(uuid, now);

        if (lastSeen != null && (now - lastSeen) < MIN_HEARTBEAT_INTERVAL_MS) {
            log.warn(
                    "Flood detected node={} intervalMs={}",
                    uuid,
                    (now - lastSeen)
            );
            return true;
        }

        return false;
    }

    private void safeDbUpdate(UUID id) {
        try {
            nodeRepository.updateNodeLastSeen(id.toString());
        } catch (Exception e) {
            log.error("DB update failed node={}", id, e);
            dbFailures.incrementAndGet();
        }
    }

    // Expose metrics for monitoring systems
    public long getAcceptedHeartbeats() {
        return acceptedHeartbeats.get();
    }

    public long getRejectedHeartbeats() {
        return rejectedHeartbeats.get();
    }

    public long getDbFailures() {
        return dbFailures.get();
    }
}