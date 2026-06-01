/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.health;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthCheck {

  private final DataSource dataSource;

  public DatabaseHealthCheck(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public boolean check() {
    try (Connection c = dataSource.getConnection();
        PreparedStatement ps = c.prepareStatement("SELECT 1");
        ResultSet rs = ps.executeQuery()) {

      return rs.next();
    } catch (Exception e) {
      return false;
    }
  }
}
