/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.vision;

import com.azeem.avisos.controller.model.vision.VisionRequest;
import com.azeem.avisos.controller.model.vision.VisionResponse;

public interface VisionService {
    VisionResponse analyze(VisionRequest visionRequest);
    boolean isAvailable();
}
