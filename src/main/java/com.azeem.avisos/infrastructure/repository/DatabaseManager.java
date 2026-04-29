/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.infrastructure.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    // The singleton instance of DB
    private static DatabaseManager instance;
    private Connection connection;
    private final String URL = "jdbc:sqlite:avisos.db";

    private DatabaseManager() {
        try {
            this.connection = DriverManager.getConnection(URL);
            initializeTables();
            System.out.println("Avisos Database Initialized.");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Avisos Database connection closed safely.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Create DataAcquisitionDevice Inventory Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS dataAcquisitionDevices (
                    id TEXT PRIMARY KEY,
                    type TEXT NOT NULL,
                    status TEXT NOT NULL,
                    battery_level REAL DEFAULT 100.0
                )
            """);

            // Create Alarm Log Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS alarm_logs (
                    id TEXT PRIMARY KEY,
                    device_type TEXT,
                    device_id TEXT,
                    alarm_status TEXT,
                    alarm_severity TEXT,
                    timestamp TEXT
                )
            """);

            // Create User Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(100) PRIMARY KEY,
                    password_hash TEXT NOT NULL
                )
            """);

            // Create Receivers Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS subscribers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    status TEXT DEFAULT 'ACTIVE'
                )
            """);
        }
    }
}
