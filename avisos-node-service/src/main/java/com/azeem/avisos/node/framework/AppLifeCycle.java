/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.framework;

import com.azeem.avisos.node.service.NodeRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for the lifecycle of the Avisos edge node application.
 *
 * <p>Creates the container, runs aspect processing, and provides access to the {@link NodeRuntime}
 * for startup and shutdown orchestration.
 */
public class AppLifeCycle {
  private static final Logger log = LoggerFactory.getLogger(AppLifeCycle.class);
  private AppContainer container;

  /** Initialize the application -- creates the container and wires all components. */
  public void init() {
    container = new AppContainer();
    AspectProcessor processor = new AspectProcessor(container.getRegistry());
    processor.applyAspects();
    container.init();
    log.info("Avisos node lifecycle initialized");
  }

  /** Returns the node runtime for starting the main process loops. */
  public NodeRuntime getRuntime() {
    return container.get(NodeRuntime.class);
  }

  /** Gracefully shuts down the node runtime and releases resources. */
  public void close() {
    NodeRuntime runtime = container.get(NodeRuntime.class);
    if (runtime != null) {
      runtime.stop();
    }
    log.info("Avisos node lifecycle closed");
  }
}
