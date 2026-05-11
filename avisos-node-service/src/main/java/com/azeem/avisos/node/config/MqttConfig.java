/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.config;

/**
 * MQTT network configuration.
 *
 * @param brokerUrl The MQTT broker connection URL.
 */
public record MqttConfig(String brokerUrl, String topic) {}
