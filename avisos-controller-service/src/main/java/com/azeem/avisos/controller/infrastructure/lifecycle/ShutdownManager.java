/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the "Exit Strategy" for the Controller.
 */
public class ShutdownManager {
    private static final Logger log = LoggerFactory.getLogger(ShutdownManager.class);
    private final List<Runnable> shutdownTasks = new CopyOnWriteArrayList<>();

    public ShutdownManager addTask(Runnable task) {
        shutdownTasks.add(task);
        return this;
    }

    public void initiate() {
        log.info("Commencing graceful shutdown of Avisos Controller...");
        for (Runnable task : shutdownTasks) {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Error during shutdown task: {}", e.getMessage());
            }
        }
        log.info("Shutdown complete. All resources released.");
    }
}
