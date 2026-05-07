/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.node;

import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.repository.NodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class SimpleNodeService implements NodeService {
    private static final Logger log = LoggerFactory.getLogger(SimpleNodeService.class);
    NodeRepository nodeRepository;

    public SimpleNodeService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    @Override
    public void updateNodeHeartbeat(NodeRecord nodeRecord) {
        nodeRepository.updateNodeLastSeen(nodeRecord.uuid().toString());
    }

    @Override
    public void checkStaleNodes() {
        int offlineCount = nodeRepository.markStaleNodesOffline(60); // 60 second threshold
        if (offlineCount > 0) {
            log.warn("Marked {} devices as OFFLINE due to heartbeat timeout.", offlineCount);
        }
    }

    @Override
    public List<UUID> getRegisteredNodes() {
        List<String> strings  = nodeRepository.getRegisteredNodeUuids();
        return strings.stream().map(UUID::fromString).toList();
    }

    @Override
    public void registerHeartbeat(UUID uuid) {
        nodeRepository.updateNodeLastSeen(uuid.toString());
    }
}
