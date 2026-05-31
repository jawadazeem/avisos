/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Telemetry ingress pipeline: receives raw MQTT messages from edge nodes, deserializes them into
 * domain objects, and drives the analysis workflow.
 *
 * <p>The processing flow for each incoming message is:
 *
 * <ol>
 *   <li>{@link com.azeem.avisos.controller.service.ingress.MqttIngressAdapter} converts a raw Paho
 *       MQTT message into an {@code IngressMessage} (topic + payload + timestamp).
 *   <li>{@link com.azeem.avisos.controller.service.ingress.TelemetryIngressHandler} deserializes
 *       the JSON payload into a {@code TelemetryPacketDto}, updates the sending node's heartbeat,
 *       and if the packet is not a heartbeat, forwards image data to the vision service for threat
 *       analysis. Detected threats above severity NONE are persisted as alarms.
 * </ol>
 *
 * <p>Heartbeat packets are special-cased: they update node state but do not trigger vision
 * analysis. Malformed packets are discarded with an error log rather than crashing the handler.
 */
package com.azeem.avisos.controller.service.ingress;
