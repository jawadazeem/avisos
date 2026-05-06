/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.entity;

import java.time.LocalDateTime;

/**
 * Database entity - maps directly to users table
 */
public record UserEntity(
        String username,
        String passwordHash,
        String role,
        LocalDateTime createdAt,
        LocalDateTime lastLogin
) {}