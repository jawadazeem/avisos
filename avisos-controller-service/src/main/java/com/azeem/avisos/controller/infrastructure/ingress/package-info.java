/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * MQTT broker connectivity and message subscription using Eclipse Paho.
 *
 * <p>{@link com.azeem.avisos.controller.infrastructure.ingress.MqttIngressListener} manages the
 * connection to the MQTT broker (auto-reconnect, clean session, configurable timeout) and subscribes
 * to the telemetry topic. Each incoming message is dispatched to the ingress handler on a virtual
 * thread to avoid blocking the Paho callback thread.
 *
 * <p>Connection failures are logged but do not crash the application &mdash; the ingress layer
 * silently goes offline and waits for reconnection.
 */
package com.azeem.avisos.controller.infrastructure.ingress;
