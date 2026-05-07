/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.network.api;

public interface BufferManager {

    /**
     * Enqueue telemetry data for asynchronous processing.
     * Should apply backpressure strategy if buffer is full.
     */
    void enqueue(byte[] data);

    /**
     * Drains buffer and publishes data to downstream system.
     */
    void drain();

    /**
     * Starts background processing loop (if applicable).
     */
    void start();

    /**
     * Stops processing gracefully.
     */
    void stop();

    /**
     * Current buffer size (for monitoring).
     */
    int size();
}
