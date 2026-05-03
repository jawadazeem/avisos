/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import com.azeem.avisos.controller.config.DatabaseConfig;
import com.azeem.avisos.controller.exceptions.CriticalInfrastructureException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import io.github.cdimascio.dotenv.Dotenv;

import javax.sql.DataSource;

public class JdbiProvider {

    private static volatile Jdbi jdbi;

    public static synchronized Jdbi getJdbi(DatabaseConfig config) {
        if (jdbi != null) {
            return jdbi;
        }

        try {
            Dotenv dotenv = Dotenv.load();
            String key = dotenv.get("DATABASE_ENCRYPTION_KEY");

            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.url());

            hikariConfig.setMaximumPoolSize(5);
            hikariConfig.setMinimumIdle(1);
            hikariConfig.setPoolName("avisos-sqlite-pool");

            HikariDataSource hikari = new HikariDataSource(hikariConfig);

            DataSource encryptingDataSource = new EncryptingDataSource(hikari, key);

            jdbi = Jdbi.create(encryptingDataSource);
            jdbi.installPlugin(new SqlObjectPlugin());

            return jdbi;

        } catch (Exception e) {
            throw new CriticalInfrastructureException(
                    "Failed to initialize database: " + e.getMessage()
            );
        }
    }
}
