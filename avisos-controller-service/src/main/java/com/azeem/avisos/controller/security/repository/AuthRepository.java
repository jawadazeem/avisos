/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.repository;

import com.azeem.avisos.controller.security.entity.UserEntity;
import com.azeem.avisos.controller.security.mapper.UserMapper;
import com.azeem.avisos.controller.security.model.UserRecord;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface AuthRepository {

    @SqlUpdate("""
        CREATE TABLE IF NOT EXISTS users (
            username TEXT PRIMARY KEY,
            password_hash TEXT NOT NULL,
            role TEXT DEFAULT 'OPERATOR',
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            last_login DATETIME
        )
    """)
    void initUserTable();

    @SqlQuery("""
        SELECT username, password_hash AS passwordHash, role, created_at AS createdAt, last_login AS lastLogin
        FROM users 
        WHERE username = :username
    """)
    @RegisterConstructorMapper(UserEntity.class)
    Optional<UserEntity> findByUsernameEntity(@Bind("username") String username);

    default Optional<UserRecord> findByUsername(String username) {
        return findByUsernameEntity(username).map(UserMapper::toDomain);
    }

    @SqlUpdate("UPDATE users SET password_hash = :newHash WHERE username = :username")
    void updatePassword(@Bind("username") String username, @Bind("newHash") String newHash);

    @SqlUpdate("INSERT INTO users (username, password_hash, role) VALUES (:username, :hash, :role)")
    void createUser(@Bind("username") String username, @Bind("hash") String hash, @Bind("role") String role);

    @SqlUpdate("DELETE FROM users WHERE username = :username")
    void deleteUser(@Bind("username") String username);

    @SqlQuery("SELECT COUNT(*) FROM users")
    int countUsers();
}
