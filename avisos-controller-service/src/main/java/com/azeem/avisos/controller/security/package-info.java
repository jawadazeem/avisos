/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Authentication subsystem using Argon2id password hashing and a thread-local security context.
 *
 * <p>Key components:
 *
 * <ul>
 *   <li>{@link com.azeem.avisos.controller.security.service.AuthService} &mdash; handles user
 *       creation, password verification (Argon2id with 3 iterations, 64 MB memory, 1 thread), and
 *       account management. All password mutations require the old password for verification.
 *   <li>{@link com.azeem.avisos.controller.security.model.SecurityContext} &mdash; stores the
 *       authenticated user in an {@code InheritableThreadLocal} so child virtual threads inherit the
 *       caller's identity. Must be cleared after use to prevent memory leaks.
 * </ul>
 *
 * <p>On first run, when no users exist in the database, the CLI triggers a bootstrap flow to create
 * the initial operator account. Role-based access control fields are stored but not yet enforced.
 */
package com.azeem.avisos.controller.security;
