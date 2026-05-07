/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.config;

/**
 * Root application configuration.
 *
 * @param node    Node-specific configuration.
 * @param mqtt MQTT/network configuration.
 */
public record AppConfig(
        NodeConfig node,
        MqttConfig mqtt
) {}