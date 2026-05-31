/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Interactive command-line interface for the controller service, built on JLine.
 *
 * <p>{@link com.azeem.avisos.controller.service.cli.JLineCliService} runs in two phases:
 *
 * <ol>
 *   <li><b>Authentication</b> &mdash; blocks until the user logs in. On first run (no users in the
 *       database), a bootstrap flow prompts the operator to create the initial admin account.
 *   <li><b>Command loop</b> &mdash; reads input, resolves a command from the registry, and executes
 *       it asynchronously on a virtual thread.
 * </ol>
 *
 * <p>The CLI thread is non-daemon, so it keeps the JVM alive after all background services start.
 * Commands are registered in {@code AppContainer.init()} and implement the {@code Command}
 * interface. See the {@code command/impl/} sub-package for available commands (about, alarms, exit,
 * health, help, inspect, nodes, purge, stats).
 */
package com.azeem.avisos.controller.service.cli;
