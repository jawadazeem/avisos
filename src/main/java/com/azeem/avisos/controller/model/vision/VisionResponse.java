/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.vision;

import java.util.List;

/**
 * Response model for vision processing results. Can be extended in the future to include
 * additional metadata such as confidence scores, bounding boxes, etc.
 * @param labels List of labels detected in the image.
 */
public record VisionResponse(
        List<String> labels
) {}
