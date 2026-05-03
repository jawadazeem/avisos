/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.vision;

import com.azeem.avisos.controller.infrastructure.vision.VisionClient;
import com.azeem.avisos.controller.model.vision.VisionRequest;
import com.azeem.avisos.controller.model.vision.VisionResponse;

/**
 * This class implements the VisionService interface and uses a VisionClient to send requests to the vision service.
 */
public class CodeProjectVisionService implements VisionService {
    private final VisionClient visionClient;

    public CodeProjectVisionService(VisionClient visionClient) {
        this.visionClient = visionClient;
    }

    @Override
    public VisionResponse analyze(VisionRequest visionRequest) {
        return visionClient.sendRequest(visionRequest);
    }

    @Override
    public boolean isAvailable() {
        return visionClient.isAvailable();
    }
}
