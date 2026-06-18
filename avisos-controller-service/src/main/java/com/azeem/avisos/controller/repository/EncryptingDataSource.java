/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.repository;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Legacy SQLCipher experiment kept for reference only.
 *
 * <p>Avisos currently uses a plain SQLite JDBC datasource. Do not wire this class into production
 * configuration unless the project adopts a SQLCipher-capable JDBC driver and a real encrypted DB
 * migration path.
 */
@Deprecated(forRemoval = false)
public class EncryptingDataSource implements DataSource {

  private final DataSource delegate;
  private final String key;

  public EncryptingDataSource(DataSource delegate, String key) {
    this.delegate = delegate;
    this.key = key;
  }

  @Override
  public Connection getConnection() throws SQLException {
    Connection conn = delegate.getConnection();
    applyKey(conn);
    enableWal(conn);
    return conn;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    Connection conn = delegate.getConnection(username, password);
    applyKey(conn);
    enableWal(conn);
    return conn;
  }

  private void applyKey(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("PRAGMA key = '" + key.replace("'", "''") + "'");
    }
  }

  private void enableWal(Connection conn) throws SQLException {
    try (Statement stmt = conn.createStatement()) {
      stmt.execute("PRAGMA journal_mode=WAL;");
    }
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return delegate.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return delegate.isWrapperFor(iface);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return delegate.getLogWriter();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    delegate.setLogWriter(out);
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    delegate.setLoginTimeout(seconds);
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    return delegate.getLoginTimeout();
  }

  @Override
  public Logger getParentLogger() {
    try {
      return delegate.getParentLogger();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
