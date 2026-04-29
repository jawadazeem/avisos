/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;

import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import java.util.List;

public interface DeviceMonitoringRepository {

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS devices (
            uuid TEXT PRIMARY KEY,
            name TEXT,
            last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    void initDeviceTable();

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS telemetry_audit (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            device_uuid TEXT,
            packet_type TEXT,
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    void initAuditTable();

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS alarms (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            device_uuid TEXT NOT NULL,
            severity TEXT NOT NULL,
            reason TEXT,
            status TEXT DEFAULT 'ACTIVE',
            triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            resolved_at TIMESTAMP
        )
    """)
    void initAlarmTable();

    @SqlUpdate("INSERT OR REPLACE INTO devices (uuid, name, last_seen) VALUES (:uuid, :name, CURRENT_TIMESTAMP)")
    void updateDeviceHeartbeat(@Bind("uuid") String uuid, @Bind("name") String name);

    @SqlUpdate("INSERT INTO telemetry_audit (device_uuid, packet_type) VALUES (:uuid, :type)")
    void logTelemetry(@Bind("uuid") String uuid, @Bind("type") String type);

    @SqlQuery("SELECT uuid FROM devices")
    List<String> getRegisteredDevices();

    @SqlUpdate("INSERT INTO alarms (device_uuid, severity, reason) VALUES (:uuid, :sev, :msg)")
    @GetGeneratedKeys
    long triggerAlarm(@Bind("uuid") String uuid, @Bind("sev") String sev, @Bind("msg") String msg);

    @SqlUpdate("UPDATE alarms SET status = 'RESOLVED', resolved_at = CURRENT_TIMESTAMP WHERE id = :id")
    void resolveAlarm(@Bind("id") long id);

    @SqlQuery("SELECT * FROM alarms WHERE status = 'ACTIVE' ORDER BY triggered_at DESC")
    @RegisterConstructorMapper(AlarmRecord.class)
    List<AlarmRecord> getActiveAlarms();
}
