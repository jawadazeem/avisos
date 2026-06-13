/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.azeem.avisos.controller.model.alarm.AlarmRecord;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AlarmRepository {

  @SqlUpdate(
      """
        CREATE TABLE IF NOT EXISTS alarms (
            id TEXT PRIMARY KEY,
            device_uuid TEXT NOT NULL,
            severity TEXT NOT NULL,
            reason TEXT,
            status TEXT NOT NULL,
            triggered_at TEXT NOT NULL,
            resolved_at TEXT,
            s3_image_key TEXT
        )
    """)
  void initAlarmTable();

  @SqlQuery("SELECT COUNT(*) FROM pragma_table_info('alarms') WHERE name = :columnName")
  int countColumn(@Bind("columnName") String columnName);

  @SqlUpdate("ALTER TABLE alarms ADD COLUMN s3_image_key TEXT")
  void addS3ImageKeyColumn();

  @SqlUpdate(
      """
        INSERT INTO alarms (
            id, device_uuid, severity, reason, status, triggered_at, s3_image_key
        )
        VALUES (
            :id, :deviceUuid, :severity, :reason, :status, :triggeredAtTimestamp, :s3ImageKey
        )
    """)
  void triggerAlarm(@BindMethods AlarmRecord alarm);

  @SqlUpdate(
      """
        UPDATE alarms
        SET status = 'RESOLVED',
            resolved_at = CURRENT_TIMESTAMP
        WHERE id = :id
    """)
  void resolveAlarm(@Bind("id") java.util.UUID id);

  @SqlUpdate(
      """
        UPDATE alarms
        SET s3_image_key = :s3ImageKey
        WHERE id = :id
    """)
  void updateS3ImageKey(@Bind("id") java.util.UUID id, @Bind("s3ImageKey") String s3ImageKey);

  @SqlQuery(
      """
        SELECT
            id,
            device_uuid AS deviceUuid,
            severity,
            reason,
            status,
            triggered_at AS triggeredAtTimestamp,
            resolved_at AS resolvedAtTimestamp,
            s3_image_key AS s3ImageKey
        FROM alarms
        WHERE status = 'ACTIVE'
    """)
  @RegisterConstructorMapper(AlarmRecord.class)
  List<AlarmRecord> getActiveAlarms();

  @SqlQuery(
      """
        SELECT
            id,
            device_uuid AS deviceUuid,
            severity,
            reason,
            status,
            triggered_at AS triggeredAtTimestamp,
            resolved_at AS resolvedAtTimestamp,
            s3_image_key AS s3ImageKey
        FROM alarms
        WHERE id = :id
    """)
  @RegisterConstructorMapper(AlarmRecord.class)
  Optional<AlarmRecord> findById(@Bind("id") UUID id);
}
