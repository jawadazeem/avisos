/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Data access layer using JDBI SQL Object interfaces over an encrypted SQLite database.
 *
 * <p>{@link com.azeem.avisos.controller.repository.JdbiProvider} initializes a singleton {@code
 * Jdbi} instance backed by a HikariCP connection pool. Each connection applies SQLCipher encryption
 * ({@code PRAGMA key}) and enables WAL mode for concurrent read performance via {@link
 * com.azeem.avisos.controller.repository.EncryptingDataSource}.
 *
 * <p>Repository interfaces ({@code AlarmRepository}, {@code NodeRepository}, {@code
 * TelemetryRepository}) are JDBI SQL Objects created via {@code jdbi.onDemand()} &mdash; they
 * acquire and release connections automatically per method call. Default methods in the interfaces
 * provide entity-to-domain mapping.
 *
 * <p>The encryption key is loaded from the {@code .env} file ({@code DATABASE_ENCRYPTION_KEY}).
 */
package com.azeem.avisos.controller.repository;
