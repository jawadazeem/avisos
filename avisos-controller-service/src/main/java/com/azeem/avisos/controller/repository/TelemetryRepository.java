/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface TelemetryRepository {
    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS telemetry_audit (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            device_uuid TEXT,
            packet_type TEXT,
            timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    void initAuditTable();

    @SqlUpdate("INSERT INTO telemetry_audit (device_uuid, packet_type) VALUES (:uuid, :type)")
    void logTelemetry(@Bind("uuid") String uuid, @Bind("type") String type);

}
