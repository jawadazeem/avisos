/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * AOP-style annotations for audit logging and performance monitoring, without Spring.
 *
 * <ul>
 *   <li>{@link com.azeem.avisos.controller.instrumentation.ServiceAudit} &mdash; marks methods for
 *       audit trails. Scanned at startup by {@code AspectProcessor} which logs annotated methods,
 *       but does not create runtime proxies.
 *   <li>{@link com.azeem.avisos.controller.instrumentation.Timed} &mdash; marks methods for
 *       latency monitoring with a configurable threshold (default 50 ms). Enforced at runtime by
 *       {@code PerformanceHandler} which logs a warning when execution exceeds the threshold.
 * </ul>
 */
package com.azeem.avisos.controller.instrumentation;
