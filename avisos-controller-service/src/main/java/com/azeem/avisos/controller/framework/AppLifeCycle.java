/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.framework;

import com.azeem.avisos.controller.infrastructure.lifecycle.ShutdownManager;

/**
 * Responsible for the lifecycle of the entire application.
 *
 * <p>This class creates and sets up the container. It also gracefully shuts down the application
 * using {@link ShutdownManager}.
 */
public class AppLifeCycle {
  private AppContainer container;

  /** Start application */
  public void init() {
    container = new AppContainer();
    AspectProcessor aspectProcessor = new AspectProcessor(container.getClassObjectRegistry());
    aspectProcessor.applyAspects();
    container.init();
    ConfigLoader configLoader = new ConfigLoader();
  }

  /** Shutdown application using {@link ShutdownManager} */
  public void close() {
    ShutdownManager shutdownManager =
        (ShutdownManager) container.getClassObjectRegistry().get(ShutdownManager.class);
  }
}
