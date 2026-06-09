/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import com.azeem.avisos.controller.repository.AlarmRepository;
import com.azeem.avisos.controller.repository.EncryptingDataSource;
import com.azeem.avisos.controller.repository.NodeRepository;
import com.azeem.avisos.controller.repository.TelemetryRepository;
import com.azeem.avisos.controller.security.repository.AuthRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import javax.sql.DataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Replaces {@code JdbiProvider} -- creates the DataSource, Jdbi, and all JDBI repositories. */
@Configuration
public class JdbiConfiguration {

  @Bean
  public DataSource avisosDataSource(AvisosDatabaseProperties dbProps) {
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    String key = System.getenv("DATABASE_ENCRYPTION_KEY");
    if (key == null || key.isBlank()) {
      key = dotenv.get("DATABASE_ENCRYPTION_KEY");
    }
    if (key == null || key.isBlank()) {
      throw new IllegalStateException("DATABASE_ENCRYPTION_KEY must be configured");
    }

    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(dbProps.url());
    hikariConfig.setMaximumPoolSize(5);
    hikariConfig.setMinimumIdle(1);
    hikariConfig.setPoolName("avisos-sqlite-pool");

    HikariDataSource hikari = new HikariDataSource(hikariConfig);
    return new EncryptingDataSource(hikari, key);
  }

  @Bean
  public Jdbi jdbi(DataSource avisosDataSource) {
    Jdbi jdbi = Jdbi.create(avisosDataSource);
    jdbi.installPlugin(new SqlObjectPlugin());
    return jdbi;
  }

  @Bean
  public AlarmRepository alarmRepository(Jdbi jdbi) {
    AlarmRepository repo = jdbi.onDemand(AlarmRepository.class);
    repo.initAlarmTable();
    return repo;
  }

  @Bean
  public NodeRepository nodeRepository(Jdbi jdbi) {
    NodeRepository repo = jdbi.onDemand(NodeRepository.class);
    repo.initNodeTable();
    return repo;
  }

  @Bean
  public TelemetryRepository telemetryRepository(Jdbi jdbi) {
    TelemetryRepository repo = jdbi.onDemand(TelemetryRepository.class);
    repo.initAuditTable();
    return repo;
  }

  @Bean
  public AuthRepository authRepository(Jdbi jdbi) {
    AuthRepository repo = jdbi.onDemand(AuthRepository.class);
    repo.initUserTable();
    return repo;
  }
}
