/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.config;

import com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Health monitoring infrastructure: virtual thread executor and periodic health check scheduler.
 */
@Configuration
public class HealthConfiguration {

  /** Virtual thread executor used by {@link SystemHealthMonitor} for async health checks. */
  @Bean
  public java.util.concurrent.ExecutorService healthExecutor() {
    return Executors.newVirtualThreadPerTaskExecutor();
  }

  /** Scheduler that runs system health checks every 10 seconds. */
  @Bean(destroyMethod = "shutdown")
  public ScheduledExecutorService healthScheduler(SystemHealthMonitor monitor) {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleWithFixedDelay(
        () -> {
          try {
            monitor.checkSystemHealth();
          } catch (Exception e) {
            e.printStackTrace();
          }
        },
        0,
        10,
        TimeUnit.SECONDS);
    return scheduler;
  }
}
