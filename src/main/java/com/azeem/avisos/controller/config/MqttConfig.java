/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

public class MqttConfig {
    private String controllerClientId;
    private String broker;
    private String topic;
    private int connectionTimeout;
    private boolean cleanSession;
    private boolean automaticReconnect;

    public MqttConfig() {}

    public String getControllerClientId() {
        return controllerClientId;
    }

    public void setControllerClientId(String controllerClientId) {
        this.controllerClientId = controllerClientId;
    }

    public String getBroker() {
        return broker;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean isAutomaticReconnect() {
        return automaticReconnect;
    }

    public void setAutomaticReconnect(boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }
}
