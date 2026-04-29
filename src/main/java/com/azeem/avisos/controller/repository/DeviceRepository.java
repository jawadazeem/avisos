/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import java.util.List;

public interface DeviceRepository {

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS devices (
            uuid TEXT PRIMARY KEY,
            name TEXT,
            last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    """)
    void initDeviceTable();

    @SqlUpdate("INSERT OR REPLACE INTO devices (uuid, name, last_seen) VALUES (:uuid, :name, CURRENT_TIMESTAMP)")
    void updateDeviceHeartbeat(@Bind("uuid") String uuid, @Bind("name") String name);

    @SqlQuery("SELECT uuid FROM devices")
    List<String> getRegisteredDevices();
}
