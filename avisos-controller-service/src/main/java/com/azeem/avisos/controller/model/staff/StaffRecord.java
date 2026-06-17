/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.staff;

import java.time.LocalDateTime;

public record StaffRecord(
    String staffId,
    String name,
    String email,
    String phone,
    String role,
    String jurisdiction,
    String primaryZone,
    String shift,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
