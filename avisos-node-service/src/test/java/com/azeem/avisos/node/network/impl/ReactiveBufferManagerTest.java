/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.network.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.azeem.avisos.node.config.MqttConfig;
import com.azeem.avisos.node.network.api.MqttProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifies buffered telemetry enqueue, drain, metrics, and lifecycle behavior. */
@ExtendWith(MockitoExtension.class)
class ReactiveBufferManagerTest {

  @Mock MqttProvider mqttProvider;

  private ReactiveBufferManager bufferManager;
  private ExecutorService workerPool;
  private ScheduledExecutorService scheduler;
  private MqttConfig mqttConfig;

  @BeforeEach
  void setUp() {
    mqttConfig = new MqttConfig("tcp://localhost:1883", "avisos/telemetry");
    workerPool = Executors.newSingleThreadExecutor();
    scheduler = Executors.newSingleThreadScheduledExecutor();
    bufferManager = new ReactiveBufferManager(mqttProvider, mqttConfig, workerPool, scheduler);
  }

  @AfterEach
  void tearDown() {
    bufferManager.stop();
    workerPool.shutdownNow();
    scheduler.shutdownNow();
  }

  @Test
  void enqueue_shouldAcceptDataWhenRunning() {
    bufferManager.start();

    bufferManager.enqueue(new byte[]{1, 2, 3});

    assertEquals(1, bufferManager.size());
    assertEquals(1, bufferManager.getAccepted());
    assertEquals(0, bufferManager.getDropped());
  }

  @Test
  void enqueue_shouldDiscardDataWhenNotRunning() {
    bufferManager.enqueue(new byte[]{1, 2, 3});

    assertEquals(0, bufferManager.size());
    assertEquals(0, bufferManager.getAccepted());
  }

  @Test
  void drain_shouldPublishBufferedMessages() throws Exception {
    bufferManager.start();
    bufferManager.enqueue(new byte[]{10, 20});

    bufferManager.drain();

    // Allow worker pool to process
    Thread.sleep(100);
    verify(mqttProvider, timeout(1000)).publish(eq("avisos/telemetry"), any(byte[].class));
  }

  @Test
  void drain_shouldDoNothingWhenBufferEmpty() {
    bufferManager.start();

    bufferManager.drain();

    verifyNoInteractions(mqttProvider);
  }

  @Test
  void drain_shouldNotDrainWhenStopped() {
    bufferManager.start();
    bufferManager.enqueue(new byte[]{1});
    bufferManager.stop();

    bufferManager.drain();

    // Payload stays in buffer, not published
    assertEquals(1, bufferManager.size());
  }

  @Test
  void stop_shouldPreventFurtherEnqueues() {
    bufferManager.start();
    bufferManager.stop();

    bufferManager.enqueue(new byte[]{1, 2, 3});

    assertEquals(0, bufferManager.size());
  }

  @Test
  void start_shouldBeIdempotent() {
    bufferManager.start();
    bufferManager.start();

    bufferManager.enqueue(new byte[]{1});
    assertEquals(1, bufferManager.getAccepted());
  }

  @Test
  void metrics_shouldTrackAcceptedCount() {
    bufferManager.start();

    bufferManager.enqueue(new byte[]{1});
    bufferManager.enqueue(new byte[]{2});
    bufferManager.enqueue(new byte[]{3});

    assertEquals(3, bufferManager.getAccepted());
    assertEquals(0, bufferManager.getDropped());
  }

  @Test
  void size_shouldReflectCurrentBufferOccupancy() {
    bufferManager.start();
    assertEquals(0, bufferManager.size());

    bufferManager.enqueue(new byte[]{1});
    bufferManager.enqueue(new byte[]{2});

    assertEquals(2, bufferManager.size());
  }

  @Test
  void metrics_shouldTrackPublishedOnSuccess() throws Exception {
    bufferManager.start();
    bufferManager.enqueue(new byte[]{1});

    bufferManager.drain();
    Thread.sleep(200);

    assertEquals(1, bufferManager.getPublished());
    assertEquals(0, bufferManager.getFailedPublishes());
  }

  @Test
  void metrics_shouldTrackFailuresOnPublishError() throws Exception {
    doThrow(new RuntimeException("broker down")).when(mqttProvider)
        .publish(anyString(), any(byte[].class));
    bufferManager.start();
    bufferManager.enqueue(new byte[]{1});

    bufferManager.drain();
    Thread.sleep(200);

    assertTrue(bufferManager.getFailedPublishes() > 0);
  }
}
