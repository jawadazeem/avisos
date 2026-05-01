/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.IngressData;

import java.time.Instant;

public record IngressMessage(
        String source,
        byte[] payload,
        Instant timestamp
) {}
