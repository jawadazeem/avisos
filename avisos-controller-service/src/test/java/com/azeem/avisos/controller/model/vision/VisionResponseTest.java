/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.model.vision;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class VisionResponseTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void shouldIgnoreUnknownCodeProjectFields() throws Exception {
    String response =
        """
        {
          "success": true,
          "message": "Found person",
          "count": 1,
          "predictions": [
            {
              "label": "person",
              "confidence": 0.884,
              "x_min": 10,
              "y_min": 20,
              "x_max": 30,
              "y_max": 40,
              "userid": "",
              "extra": "ignored"
            }
          ],
          "inferenceMs": 212,
          "executionProvider": "CPUExecutionProvider"
        }
        """;

    VisionResponse visionResponse = objectMapper.readValue(response, VisionResponse.class);

    assertTrue(visionResponse.success());
    assertEquals(1, visionResponse.predictions().size());
    assertEquals("person", visionResponse.predictions().getFirst().label());
    assertEquals(0.884, visionResponse.predictions().getFirst().confidence());
  }
}
