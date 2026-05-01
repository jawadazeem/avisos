/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.vision;

import java.util.List;

/**
 * Can be implemented using AWS Rekognition or any other image recognition service.
 */
public interface VisionClient {
    List<String> detectLabels(byte[] imageBytes);
}
