/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Core node services: heartbeat telemetry generation and node lifecycle management.
 *
 * <ul>
 *   <li>{@link com.azeem.avisos.node.service.HeartbeatService} &mdash; builds a {@code
 *       TelemetryPacketDto} with current battery level, node metadata, and a simulated 1 KB sensor
 *       payload, then publishes it as JSON over MQTT.
 *   <li>{@link com.azeem.avisos.node.service.NodeRuntime} &mdash; orchestrates three concurrent
 *       virtual-thread loops: a <b>connection supervisor</b> (MQTT connect with exponential backoff
 *       up to 60 s), a <b>heartbeat loop</b> (default every 30 s), and a <b>watchdog</b> (battery
 *       monitoring every 60 s, warns below 15%).
 * </ul>
 *
 * <p>The runtime follows a strict state machine: {@code SHUTDOWN -> STARTING -> RUNNING -> STOPPING
 * -> SHUTDOWN}. On shutdown, the executor waits up to 5 seconds for threads to terminate.
 */
package com.azeem.avisos.node.service;
