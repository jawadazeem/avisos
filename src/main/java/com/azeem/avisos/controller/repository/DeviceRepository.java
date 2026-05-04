/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.azeem.avisos.controller.entity.device.DeviceEntity;
import com.azeem.avisos.controller.model.device.DeviceRecord;
import com.azeem.avisos.controller.mapper.device.DeviceMapper;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository {

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS devices (
            uuid TEXT PRIMARY KEY,
            name TEXT,
            type TEXT,
            status TEXT,
            battery_level REAL,
            last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    void initDeviceTable();

    @SqlQuery("""
        SELECT 
            uuid, name, type, status, battery_level AS batteryLevel, last_seen AS lastSeen 
        FROM devices 
        WHERE uuid = :uuid
    """)
    @RegisterConstructorMapper(DeviceEntity.class)
    DeviceEntity getDeviceEntity(@Bind("uuid") String uuid);

    default DeviceRecord getDevice(UUID uuid) {
        DeviceEntity entity = getDeviceEntity(uuid.toString());
        return DeviceMapper.toDomain(entity);
    }

    @SqlQuery("""
        SELECT 
            uuid, name, type, status, battery_level AS batteryLevel, last_seen AS lastSeen 
        FROM devices
    """)
    @RegisterConstructorMapper(DeviceEntity.class)
    List<DeviceEntity> getAllDeviceEntities();

    default List<DeviceRecord> getAllDevices() {
        return getAllDeviceEntities().stream()
                .map(DeviceMapper::toDomain)
                .toList();
    }

    @SqlUpdate("INSERT OR REPLACE INTO devices (uuid, name, type, status, battery_level, last_seen)" +
            " VALUES (:uuid, :name, :type, :status, :batteryLevel, CURRENT_TIMESTAMP)")
    void saveDevice(@Bind("uuid") String uuid, @Bind("name") String name, @Bind("type")
        String type, @Bind("status") String status, @Bind("batteryLevel") double batteryLevel);

    default void saveDevice(DeviceRecord device) {
        DeviceEntity entity = DeviceMapper.toEntity(device);
        saveDevice(entity.uuid(), entity.name(), entity.type(), entity.status(), entity.batteryLevel());
    }

    @SqlUpdate("DELETE FROM devices WHERE uuid = :uuid")
    void removeDevice(@Bind("uuid") String uuid);

    @SqlUpdate("UPDATE devices SET last_seen = CURRENT_TIMESTAMP WHERE uuid = :uuid")
    void updateDeviceLastSeen(@Bind("uuid") String uuid);

    @SqlQuery("SELECT uuid FROM devices")
    List<String> getRegisteredDeviceUuids();

    @SqlUpdate("UPDATE devices SET status = 'OFFLINE' WHERE status != 'OFFLINE' " +
            "AND (unixepoch('now') - unixepoch(last_seen)) > :thresholdSeconds")
    int markStaleDevicesOffline(@Bind("thresholdSeconds") int thresholdSeconds);
}