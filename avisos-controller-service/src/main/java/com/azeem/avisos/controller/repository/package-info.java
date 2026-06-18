/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Data access layer using JDBI SQL Object interfaces over SQLite.
 *
 * <p>{@link com.azeem.avisos.controller.config.JdbiConfiguration} initializes a singleton {@code
 * Jdbi} instance backed by a HikariCP connection pool.
 *
 * <p>Repository interfaces ({@code AlarmRepository}, {@code NodeRepository}, {@code
 * TelemetryRepository}) are JDBI SQL Objects created via {@code jdbi.onDemand()} &mdash; they
 * acquire and release connections automatically per method call. Default methods in the interfaces
 * provide entity-to-domain mapping.
 */
package com.azeem.avisos.controller.repository;
