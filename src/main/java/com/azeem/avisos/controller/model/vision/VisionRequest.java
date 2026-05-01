/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.vision;

/**
 * This record represents a request to the vision service, containing the payload as a byte array.
 */
public record VisionRequest(
        byte[] payload
) {}
