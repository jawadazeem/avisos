/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.network.impl;

import com.azeem.avisos.node.network.api.BufferManager;
import com.azeem.avisos.node.network.api.MqttProvider;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * Buffered telemetry manager responsible for temporarily storing and
 * regulating outgoing telemetry before it is processed or transmitted.
 *
 * <p>
 * This component is designed to provide controlled buffering under load
 * while preventing uncontrolled memory growth or thread exhaustion.
 * </p>
 *
 * <p>
 * Design goals:
 * </p>
 *
 * <ul>
 *     <li>bounded memory usage</li>
 *     <li>safe backpressure handling under high load</li>
 *     <li>controlled concurrency to prevent thread explosion</li>
 *     <li>failure isolation between processing stages</li>
 *     <li>support for observability hooks (metrics, logging, tracing)</li>
 * </ul>
 */
public class ReactiveBufferManager implements BufferManager {

    private final ArrayBlockingQueue<byte[]> buffer;
    private final MqttProvider mqttProvider;
    private final ExecutorService workerPool;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    // Metrics
    private final LongAdder accepted = new LongAdder();
    private final LongAdder dropped = new LongAdder();
    private final LongAdder published = new LongAdder();
    private final LongAdder failedPublishes = new LongAdder();

    // Config
    private static final int WORKER_THREADS = 4;
    private static final int BUFFER_SIZE = 5000;

    public ReactiveBufferManager(MqttProvider mqttProvider) {
        this.mqttProvider = mqttProvider;
        this.buffer = new ArrayBlockingQueue<>(BUFFER_SIZE);

        this.workerPool = Executors.newFixedThreadPool(
                WORKER_THREADS,
                Thread.ofVirtual().factory()
        );

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    // Producer side
    @Override
    public void enqueue(byte[] data) {
        if (!running.get()) {
            return;
        }

        if (buffer.offer(data)) {
            accepted.increment();
        } else {
            dropped.increment();

            // Backpressure strategy
            spoolToDisk(data);
        }
    }

    // Consumer side
    @Override
    public void drain() {
        if (!running.get()) return;

        byte[] payload;

        while ((payload = buffer.poll()) != null) {
            final byte[] message = payload;

            workerPool.submit(() -> process(message));
        }
    }

    private void process(byte[] payload) {
        try {
            mqttProvider.publish("avisos/telemetry/priority", payload);
            published.increment();

        } catch (Exception e) {
            failedPublishes.increment();

            // retry strategy (simple version)
            retryPublish(payload, 2);
        }
    }

    private void retryPublish(byte[] payload, int attempts) {
        for (int i = 0; i < attempts; i++) {
            try {
                mqttProvider.publish("avisos/telemetry/priority", payload);
                published.increment();
                return;

            } catch (Exception ignored) {
                // exponential backoff could be added here
            }
        }

        // final fallback
        spoolToDisk(payload);
    }

    // Lifecycle
    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) return;

        scheduler.scheduleAtFixedRate(this::drain, 0, 200, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        running.set(false);

        scheduler.shutdown();
        workerPool.shutdown();

        try {
            scheduler.awaitTermination(3, TimeUnit.SECONDS);
            workerPool.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    // Monitoring
    @Override
    public int size() {
        return buffer.size();
    }

    public long getAccepted() {
        return accepted.sum();
    }

    public long getDropped() {
        return dropped.sum();
    }

    public long getPublished() {
        return published.sum();
    }

    public long getFailedPublishes() {
        return failedPublishes.sum();
    }

    // Backpressure fallback
    private void spoolToDisk(byte[] data) {
        // Placeholder for extension:
        // - file-based queue
        // - RocksDB
        // - Kafka fallback topic
    }
}
