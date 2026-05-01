/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.model;

import java.time.LocalDateTime;

/**
 * Immutable record representing a user in the system.
 *
 * @param username     The unique username of the user.
 * @param passwordHash The hashed password for authentication.
 * @param role         The role of the user (e.g., "admin", "user").
 * @param createdAt    The timestamp when the user was created.
 */
public record UserRecord(
        String username,
        String passwordHash,
        String role,
        LocalDateTime createdAt
) {}