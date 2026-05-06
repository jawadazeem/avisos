/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

public record MqttConfig (
    String controllerClientId,
    String broker,
    String topic,
    int connectionTimeout,
    boolean cleanSession,
    boolean automaticReconnect
) {}
