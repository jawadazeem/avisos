/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.azeem.avisos.controller.entity.node.NodeEntity;
import com.azeem.avisos.controller.model.node.NodeRecord;
import com.azeem.avisos.controller.mapper.node.NodeMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface NodeRepository {

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS nodes (
            uuid TEXT PRIMARY KEY,
            name TEXT,
            type TEXT,
            status TEXT,
            battery_level REAL,
            last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    void initNodeTable();

    @SqlQuery("""
        SELECT 
            uuid, name, type, status, battery_level AS batteryLevel, last_seen AS lastSeen 
        FROM nodes 
        WHERE uuid = :uuid
    """)
    @RegisterConstructorMapper(NodeEntity.class)
    NodeEntity getNodeEntity(@Bind("uuid") String uuid);

    default NodeRecord getNode(UUID uuid) {
        NodeEntity entity = getNodeEntity(uuid.toString());
        return NodeMapper.toDomain(entity);
    }

    @SqlQuery("""
        SELECT 
            uuid, name, type, status, battery_level AS batteryLevel, last_seen AS lastSeen 
        FROM nodes
    """)
    @RegisterConstructorMapper(NodeEntity.class)
    List<NodeEntity> getAllNodeEntities();

    default List<NodeRecord> getAllNodes() {
        return getAllNodeEntities().stream()
                .map(NodeMapper::toDomain)
                .toList();
    }

    @SqlUpdate("INSERT OR REPLACE INTO nodes (uuid, name, type, status, battery_level, last_seen)" +
            " VALUES (:uuid, :name, :type, :status, :batteryLevel, CURRENT_TIMESTAMP)")
    void saveNode(@Bind("uuid") String uuid, @Bind("name") String name, @Bind("type")
        String type, @Bind("status") String status, @Bind("batteryLevel") double batteryLevel);

    default void saveNode(NodeRecord node) {
        NodeEntity entity = NodeMapper.toEntity(node);
        saveNode(entity.uuid(), entity.name(), entity.type(), entity.status(), entity.batteryLevel());
    }

    @SqlUpdate("DELETE FROM nodes WHERE uuid = :uuid")
    void removeNode(@Bind("uuid") String uuid);

    @SqlUpdate("UPDATE nodes SET last_seen = CURRENT_TIMESTAMP WHERE uuid = :uuid")
    void updateNodeLastSeen(@Bind("uuid") String uuid);

    @SqlQuery("SELECT uuid FROM nodes")
    List<String> getRegisteredNodeUuids();

    @SqlUpdate("UPDATE nodes SET status = 'OFFLINE' WHERE status != 'OFFLINE' " +
            "AND (unixepoch('now') - unixepoch(last_seen)) > :thresholdSeconds")
    int markStaleNodesOffline(@Bind("thresholdSeconds") int thresholdSeconds);
}