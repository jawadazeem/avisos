/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Node lifecycle management: tracks edge device heartbeats, maintains an in-memory registry of
 * active nodes, and detects stale/offline nodes.
 *
 * <p>{@link com.azeem.avisos.controller.service.node.SimpleNodeService} is the primary
 * implementation. It maintains a {@code ConcurrentHashMap} cache of active nodes for fast lookups,
 * with SQLite as the authoritative store. Key behaviors:
 *
 * <ul>
 *   <li><b>Flood protection</b> &mdash; heartbeats arriving faster than a configurable minimum
 *       interval are silently rejected without hitting the database.
 *   <li><b>Stale node detection</b> &mdash; a scheduled task (every 10s) marks nodes OFFLINE if
 *       their last-seen timestamp exceeds a threshold, then prunes them from the in-memory cache.
 *   <li><b>Two-tier lookup</b> &mdash; reads check the in-memory cache first, falling back to the
 *       database.
 * </ul>
 *
 * <p>This package also contains {@link
 * com.azeem.avisos.controller.service.node.PerformanceHandler}, a dynamic-proxy {@code
 * InvocationHandler} that enforces {@code @Timed} annotation thresholds at runtime.
 */
package com.azeem.avisos.controller.service.node;
