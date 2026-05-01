/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.vision;

import com.azeem.avisos.controller.model.vision.VisionRequest;
import com.azeem.avisos.controller.model.vision.VisionResponse;

/**
 * Can be implemented using AWS Rekognition or any other image recognition service.
 */
public interface VisionClient {
    VisionResponse detectLabels(VisionRequest visionRequest);
}
