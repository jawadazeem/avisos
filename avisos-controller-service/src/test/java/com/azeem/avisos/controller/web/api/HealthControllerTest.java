/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.azeem.avisos.controller.infrastructure.health.ComponentHealth;
import com.azeem.avisos.controller.infrastructure.health.HealthStatusLevel;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthMonitor;
import com.azeem.avisos.controller.infrastructure.health.SystemHealthReport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

  @Mock SystemHealthMonitor systemHealthMonitor;

  @InjectMocks HealthController healthController;

  @Test
  void getHealth_shouldRefreshThenReturnReport() {
    SystemHealthReport report =
        new SystemHealthReport(
            HealthStatusLevel.HEALTHY,
            List.of(new ComponentHealth("database", HealthStatusLevel.HEALTHY, "OK", 5)));
    when(systemHealthMonitor.checkSystemHealth()).thenReturn(report);

    SystemHealthReport result = healthController.getHealth();

    InOrder inOrder = inOrder(systemHealthMonitor);
    inOrder.verify(systemHealthMonitor).refreshHealth();
    inOrder.verify(systemHealthMonitor).checkSystemHealth();
    assertEquals(HealthStatusLevel.HEALTHY, result.overallStatus());
    assertEquals(1, result.components().size());
  }

  @Test
  void getHealth_shouldReturnDegradedReport() {
    SystemHealthReport report =
        new SystemHealthReport(
            HealthStatusLevel.DEGRADED,
            List.of(
                new ComponentHealth("database", HealthStatusLevel.HEALTHY, "OK", 3),
                new ComponentHealth("storage", HealthStatusLevel.DEGRADED, "Low disk", 1)));
    when(systemHealthMonitor.checkSystemHealth()).thenReturn(report);

    SystemHealthReport result = healthController.getHealth();

    assertEquals(HealthStatusLevel.DEGRADED, result.overallStatus());
    assertEquals(2, result.components().size());
  }
}
