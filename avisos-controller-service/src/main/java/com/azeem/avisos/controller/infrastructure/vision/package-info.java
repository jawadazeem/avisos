/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

/**
 * HTTP client for the CodeProject.AI vision detection API.
 *
 * <p>{@link com.azeem.avisos.controller.infrastructure.vision.CodeProjectVisionClient} sends image
 * data as a multipart/form-data POST request (RFC 7578) to the vision service and parses the JSON
 * response into a {@code VisionResponse}. The multipart body is constructed manually with byte
 * arrays rather than using a library. A health check endpoint ({@code /v1/module/status/vision}) is
 * used to verify vision service availability.
 */
package com.azeem.avisos.controller.infrastructure.vision;
