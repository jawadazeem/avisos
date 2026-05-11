/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.model.node;

import java.util.UUID;

/** Represents the node/device this program is running on. */
public record Node(UUID uuid, String name, String type) {}
