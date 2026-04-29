/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.UUID;

public interface AlarmRepository {
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

    @SqlUpdate("INSERT INTO alarms (id, device_uuid, severity, reason, status, triggered_at) " +
            "VALUES (:id, :deviceUuid, :severity, :reason, :status, :triggeredAt)")
    void triggerAlarm(@BindBean AlarmRecord alarm);

    @SqlUpdate("UPDATE alarms SET status = 'RESOLVED', resolved_at = CURRENT_TIMESTAMP WHERE id = :id")
    void resolveAlarm(@Bind("id") UUID id);

    @SqlQuery("SELECT * FROM alarms WHERE status = 'ACTIVE'")
    @RegisterConstructorMapper(AlarmRecord.class)
    List<AlarmRecord> getActiveAlarms();
}
