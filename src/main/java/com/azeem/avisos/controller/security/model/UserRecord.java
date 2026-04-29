/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.model;

import java.time.LocalDateTime;

public record UserRecord(
        String username,
        String passwordHash,
        String role,
        LocalDateTime createdAt
) {}