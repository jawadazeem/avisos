/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.vision;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Response model for vision processing results. Can be extended in the future to include additional
 * metadata such as bounding boxes. executionProvider indicates whether GPU or CPU was used for
 * inference
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record VisionResponse(
    boolean success, List<Prediction> predictions, int inferenceMs, String executionProvider) {}
