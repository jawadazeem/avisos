/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.mapper.node;

import com.azeem.avisos.controller.entity.node.NodeEntity;
import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.model.node.NodeStatus;

import java.util.UUID;

public class NodeMapper {
    public static NodeRecord toDomain(NodeEntity entity) {
        return new NodeRecord(
                UUID.fromString(entity.uuid()),
                entity.name(),
                entity.type(),
                NodeStatus.valueOf(entity.status()),
                entity.batteryLevel(),
                entity.lastSeen()
        );
    }

    public static NodeEntity toEntity(NodeRecord record) {
        return new NodeEntity(
                record.uuid().toString(),
                record.name(),
                record.type(),
                record.status().name(),
                record.batteryLevel(),
                record.lastSeen()
        );
    }
}
