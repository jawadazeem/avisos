/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node;

import com.azeem.avisos.node.framework.AppLifeCycle;
import com.azeem.avisos.node.service.NodeRuntime;
import java.util.concurrent.CountDownLatch;

public final class NodeApplication {

  private NodeApplication() {}

  /**
   * Application entry point.
   *
   * @param args command-line startup arguments
   */
  public static void main(String[] args) throws InterruptedException {
    AppLifeCycle lifecycle = new AppLifeCycle();
    lifecycle.init();

    NodeRuntime runtime = lifecycle.getRuntime();
    CountDownLatch shutdownLatch = new CountDownLatch(1);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  lifecycle.close();
                  shutdownLatch.countDown();
                },
                "avisos-node-shutdown"));

    runtime.start();
    shutdownLatch.await();
  }
}
