/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * Lightweight DIY IoC framework for the Avisos edge node.
 *
 * <p>This package provides a minimal inversion-of-control container purpose-built for edge devices
 * running the Avisos SCADA node service. It avoids the overhead of full frameworks like Spring
 * Boot, keeping the deployment footprint small and the startup time fast.
 *
 * <p>The four main classes mirror the architecture originally developed in the controller service:
 *
 * <ul>
 *   <li>{@link com.azeem.avisos.node.framework.AppContainer} -- bean registry and dependency wiring
 *   <li>{@link com.azeem.avisos.node.framework.AppLifeCycle} -- application lifecycle coordinator
 *   <li>{@link com.azeem.avisos.node.framework.ConfigLoader} -- YAML + environment variable config
 *   <li>{@link com.azeem.avisos.node.framework.AspectProcessor} -- annotation scanning (scaffold)
 * </ul>
 */
package com.azeem.avisos.node.framework;
