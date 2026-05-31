/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Graceful shutdown coordination for the controller service.
 *
 * <p>{@link com.azeem.avisos.controller.infrastructure.lifecycle.ShutdownManager} provides a
 * fluent-builder API for registering shutdown tasks ({@code addTask(Runnable)}). When {@code
 * initiate()} is called, tasks run sequentially &mdash; order matters &mdash; and exceptions in one
 * task do not prevent subsequent tasks from executing.
 */
package com.azeem.avisos.controller.infrastructure.lifecycle;
