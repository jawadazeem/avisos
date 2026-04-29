/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.devices.api;

import java.util.UUID;

public interface Detectorable {
    boolean detectIfNew(UUID Id);
}
