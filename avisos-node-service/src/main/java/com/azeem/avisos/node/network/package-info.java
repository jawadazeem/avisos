/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * MQTT-based networking layer for telemetry transmission from node to controller.
 *
 * <p>This package is split into two sub-packages:
 *
 * <ul>
 *   <li>{@code api/} &mdash; interfaces ({@code MqttProvider}, {@code BufferManager}) that decouple
 *       business logic from transport implementation.
 *   <li>{@code impl/} &mdash; concrete implementations: {@code PahoMqttProvider} (Eclipse Paho MQTT
 *       client with QoS 1, auto-reconnect) and {@code ReactiveBufferManager} (bounded in-memory
 *       queue with backpressure, scheduled draining every 40 ms, and retry logic).
 * </ul>
 *
 * <p>The reactive buffer provides resilience for unreliable networks: when the buffer fills (5000
 * items), new messages are dropped with a placeholder spool-to-disk call (not yet implemented).
 */
package com.azeem.avisos.node.network;
