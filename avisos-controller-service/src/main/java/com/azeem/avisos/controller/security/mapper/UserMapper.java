/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.mapper;

import com.azeem.avisos.controller.security.entity.UserEntity;
import com.azeem.avisos.controller.security.model.UserRecord;

/**
 * Maps between UserEntity (database) and UserRecord (domain model)
 */
public class UserMapper {

    public static UserRecord toDomain(UserEntity entity) {
        return new UserRecord(
                entity.username(),
                entity.passwordHash(),
                entity.role(),
                entity.createdAt()
        );
    }

    public static UserEntity toEntity(UserRecord record) {
        return new UserEntity(
                record.username(),
                record.passwordHash(),
                record.role(),
                record.createdAt(),
                null  // lastLogin not in domain model
        );
    }
}