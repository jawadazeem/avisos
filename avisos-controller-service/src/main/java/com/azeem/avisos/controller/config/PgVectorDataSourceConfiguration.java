/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Provides the primary Spring datasource used by Spring AI PgVector auto-configuration.
 *
 * <p>The controller also has a SQLite datasource named {@code avisosDataSource} for JDBI
 * repositories. Keeping this datasource primary ensures Spring AI receives PostgreSQL when it
 * initializes the PgVector extension and schema.
 */
@Configuration
public class PgVectorDataSourceConfiguration {

  @Bean
  @Primary
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties pgVectorDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "dataSource")
  @Primary
  public DataSource dataSource(
      @Qualifier("pgVectorDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }
}
