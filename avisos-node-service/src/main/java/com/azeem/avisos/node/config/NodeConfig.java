/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.config;

import java.util.UUID;

/**
 * Core node configuration.
 *
 * @param nodeId The unique identifier of the node.
 * @param name The human-readable node name.
 * @param type The functional type of the node.
 */
public record NodeConfig(UUID nodeId, String name, String type) {}
