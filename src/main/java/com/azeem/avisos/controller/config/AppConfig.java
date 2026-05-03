/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

public class AppConfig {
    private DatabaseConfig database;
    private MqttConfig mqtt;
    private VisionConfig vision;

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    public MqttConfig getMqtt() {
        return mqtt;
    }

    public void setMqtt(MqttConfig mqtt) {
        this.mqtt = mqtt;
    }

    public VisionConfig getVision() {
        return vision;
    }

    public void setVision(VisionConfig vision) {
        this.vision = vision;
    }
}
