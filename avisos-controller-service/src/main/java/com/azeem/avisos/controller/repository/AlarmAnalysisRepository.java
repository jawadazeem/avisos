/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.azeem.avisos.controller.model.alarm.AlarmAnalysisRecord;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AlarmAnalysisRepository {

  @SqlUpdate(
      """
            CREATE TABLE IF NOT EXISTS alarm_analysis (
                alarm_id TEXT PRIMARY KEY,
                analysis_text TEXT NOT NULL,
                prompt_version TEXT NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY(alarm_id) REFERENCES alarms(id) ON DELETE CASCADE
            )
        """)
  void initAnalysisTable();

  @SqlUpdate(
      """
            INSERT INTO alarm_analysis (alarm_id, analysis_text, prompt_version, created_at)
            VALUES (:alarmId, :analysisText, :promptVersion, CURRENT_TIMESTAMP)
        """)
  void saveAnalysis(@BindMethods AlarmAnalysisRecord record);

  @SqlQuery(
      """
            SELECT
                alarm_id AS alarmId,
                analysis_text AS analysisText,
                prompt_version AS promptVersion,
                created_at AS createdAt
            FROM alarm_analysis
            WHERE alarm_id = :alarmId
        """)
  @RegisterConstructorMapper(AlarmAnalysisRecord.class)
  Optional<AlarmAnalysisRecord> findByAlarmId(@Bind("alarmId") String alarmId);
}
