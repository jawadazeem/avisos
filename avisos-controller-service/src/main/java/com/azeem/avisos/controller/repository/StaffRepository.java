/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.azeem.avisos.controller.model.staff.StaffRecord;
import java.util.List;
import java.util.Optional;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindMethods;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface StaffRepository {

  @SqlUpdate(
      """
        CREATE TABLE IF NOT EXISTS staff (
            staff_id TEXT PRIMARY KEY,
            name TEXT NOT NULL,
            email TEXT NOT NULL UNIQUE,
            phone TEXT NOT NULL,
            role TEXT,
            jurisdiction TEXT,
            primary_zone TEXT,
            shift_name TEXT,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
    """)
  void initStaffTable();

  @SqlUpdate(
      """
        INSERT INTO staff (
            staff_id, name, email, phone, role, jurisdiction, primary_zone, shift_name
        )
        VALUES (
            :staffId, :name, :email, :phone, :role, :jurisdiction, :primaryZone, :shift
        )
        ON CONFLICT(staff_id) DO UPDATE SET
            name = excluded.name,
            email = excluded.email,
            phone = excluded.phone,
            role = excluded.role,
            jurisdiction = excluded.jurisdiction,
            primary_zone = excluded.primary_zone,
            shift_name = excluded.shift_name,
            updated_at = CURRENT_TIMESTAMP
    """)
  void upsert(@BindMethods StaffRecord staff);

  @SqlQuery(
      """
        SELECT
            staff_id AS staffId,
            name,
            email,
            phone,
            role,
            jurisdiction,
            primary_zone AS primaryZone,
            shift_name AS shift,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM staff
        WHERE staff_id = :staffId
    """)
  @RegisterConstructorMapper(StaffRecord.class)
  Optional<StaffRecord> findByStaffId(@Bind("staffId") String staffId);

  @SqlQuery(
      """
        SELECT
            staff_id AS staffId,
            name,
            email,
            phone,
            role,
            jurisdiction,
            primary_zone AS primaryZone,
            shift_name AS shift,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM staff
        ORDER BY name ASC
    """)
  @RegisterConstructorMapper(StaffRecord.class)
  List<StaffRecord> findAll();

  @SqlQuery(
      """
        SELECT
            staff_id AS staffId,
            name,
            email,
            phone,
            role,
            jurisdiction,
            primary_zone AS primaryZone,
            shift_name AS shift,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM staff
        WHERE lower(jurisdiction) LIKE '%' || lower(:query) || '%'
           OR lower(primary_zone) LIKE '%' || lower(:query) || '%'
           OR lower(role) LIKE '%' || lower(:query) || '%'
        ORDER BY name ASC
    """)
  @RegisterConstructorMapper(StaffRecord.class)
  List<StaffRecord> searchByJurisdiction(@Bind("query") String query);

  @SqlUpdate("DELETE FROM staff WHERE staff_id = :staffId")
  void deleteByStaffId(@Bind("staffId") String staffId);
}
