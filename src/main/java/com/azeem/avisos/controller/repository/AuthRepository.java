/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AuthRepository {

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS users (
            username TEXT PRIMARY KEY,
            password_hash TEXT NOT NULL,
            role TEXT DEFAULT 'OPERATOR', -- ADMIN, OPERATOR, VIEWER
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            last_login DATETIME
        )
    """)
    void initDeviceTable();
}
