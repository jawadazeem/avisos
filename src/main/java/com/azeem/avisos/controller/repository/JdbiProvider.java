/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

public class JdbiProvider {
    private static Jdbi jdbi;

    public static Jdbi getJdbi() {
        if (jdbi == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:avisos_core.db");
            config.setMaximumPoolSize(10); // Perfect for local Docker env
            config.setPoolName("Avisos-Pool");

            HikariDataSource ds = new HikariDataSource(config);
            jdbi = Jdbi.create(ds);
            jdbi.installPlugin(new SqlObjectPlugin()); // Enables the Interface-style Repos
        }
        return jdbi;
    }
}
