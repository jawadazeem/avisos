/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.vision;

public record Prediction(
        String label,
        double confidence,
        int x_min,
        int y_min,
        int x_max,
        int y_max
) {}
