/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.node;

import com.azeem.avisos.controller.model.node.NodeRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NodeService {
    void updateNodeHeartbeat(NodeRecord nodeRecord);
    void checkStaleNodes();
    List<UUID> getRegisteredNodes();
    void registerHeartbeat(UUID uuid);
    Optional<NodeRecord> getNode(UUID nodeId);
}
