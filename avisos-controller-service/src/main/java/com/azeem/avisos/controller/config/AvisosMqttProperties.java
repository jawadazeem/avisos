/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Binds MQTT configuration from {@code avisos.mqtt.*} in application.yml. */
@ConfigurationProperties(prefix = "avisos.mqtt")
public record AvisosMqttProperties(
    String controllerClientId,
    String broker,
    String topic,
    int connectionTimeout,
    boolean cleanSession,
    boolean automaticReconnect) {}
