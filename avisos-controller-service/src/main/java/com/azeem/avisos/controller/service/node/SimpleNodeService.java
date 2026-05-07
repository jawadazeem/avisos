/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.node;

import com.azeem.avisos.controller.entity.node.NodeEntity;
import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.model.node.NodeStatus;
import com.azeem.avisos.controller.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleNodeService implements NodeService {
    private static final Logger log = LoggerFactory.getLogger(SimpleNodeService.class);

    private final NodeRepository nodeRepository;

    // Memory Registry
    private final Map<UUID, NodeEntity> activeRegistry = new ConcurrentHashMap<>();

    // Flood Protection: Enforces a 2-second cooldown per node heartbeat
    private final Map<UUID, Long> floodProtector = new ConcurrentHashMap<>();
    private static final long MIN_HEARTBEAT_INTERVAL_MS = 2000;

    public SimpleNodeService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    /**
     * Updates node status based on a received heartbeat pulse.
     * Uses a functional approach to update the Immutable NodeEntity.
     */
    @Override
    public void registerHeartbeat(UUID uuid) {
        long now = System.currentTimeMillis();

        // DOS Protection
        if (isFlooding(uuid, now)) {
            return;
        }

        // Update memory and persist to DB
        activeRegistry.compute(uuid, (id, existing) -> {
            if (existing == null) {
                // If not in cache, try to pull from DB to hydrate memory
                existing = nodeRepository.getNodeEntity(id.toString());
            }

            if (existing != null) {
                // Use the Record's "with" pattern to generate a new state snapshot
                NodeEntity updated = existing.withHeartbeat(
                        existing.batteryLevel(),
                        NodeStatus.RESPONSIVE.name()
                );

                // Write-through to database
                nodeRepository.updateNodeLastSeen(id.toString());
                return updated;
            }

            log.warn("Pulse received from unregistered node: {}", id);
            return null;
        });
    }

    /**
     * Performs a system-wide health check. Marks nodes as OFFLINE in DB
     * and clears stale cache entries.
     */
    @Override
    public void checkStaleNodes() {
        int threshold = 60; // 60 seconds
        int affected = nodeRepository.markStaleNodesOffline(threshold);

        if (affected > 0) {
            log.info("Cleanup Task: Marked {} nodes as OFFLINE in persistence.", affected);

            // Clean up memory registry and flood protector for consistency
            LocalDateTime cutoff = LocalDateTime.now().minusSeconds(threshold);

            activeRegistry.entrySet().removeIf(entry ->
                    entry.getValue().lastSeen().isBefore(cutoff));

            floodProtector.keySet().removeIf(uuid ->
                    !activeRegistry.containsKey(uuid));
        }
    }

    @Override
    public void updateNodeHeartbeat(NodeRecord nodeRecord) {
        // Implementation for processing a full Data Object
        nodeRepository.saveNode(nodeRecord);
        activeRegistry.put(nodeRecord.uuid(), nodeRepository.getNodeEntity(nodeRecord.uuid().toString()));
    }

    @Override
    public List<UUID> getRegisteredNodes() {
        return nodeRepository.getRegisteredNodeUuids().stream()
                .map(UUID::fromString)
                .toList();
    }

    private boolean isFlooding(UUID uuid, long now) {
        Long lastSeen = floodProtector.put(uuid, now);
        return lastSeen != null && (now - lastSeen) < MIN_HEARTBEAT_INTERVAL_MS;
    }
}