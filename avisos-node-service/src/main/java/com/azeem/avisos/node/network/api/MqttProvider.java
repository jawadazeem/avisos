/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.network.api;

/**
 * Defines the contract for network telemetry transport.
 * Decouples business logic from specific MQTT implementations (e.g., Paho).
 */
public interface MqttProvider {
    void publish(String topic, byte[] payload);
    void connect() throws Exception;
    void disconnect();
}
