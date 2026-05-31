/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * High-velocity error deduplication for edge environments.
 *
 * <p>{@link com.azeem.avisos.controller.infrastructure.logging.DeduplicatingLogger} suppresses
 * repetitive errors to prevent log flooding from noisy nodes. The first occurrence of a unique
 * {@code nodeId:errorCode} pair is logged at ERROR level; subsequent duplicates are suppressed until
 * the 100th occurrence, which is logged at WARN level (and every 100th after that). The dedup cache
 * has no automatic expiry &mdash; call {@code flush()} to clear it.
 */
package com.azeem.avisos.controller.infrastructure.logging;
