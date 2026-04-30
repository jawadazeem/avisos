/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.rekognition;

import com.azeem.avisos.controller.exceptions.CannotDetectLabelsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class CodeProjectVisionService implements VisionService {
    private static final Logger log = LoggerFactory.getLogger(CodeProjectVisionService.class);
    private final HttpClient httpClient;
    private final String apiUrl;

    public CodeProjectVisionService(String apiUrl) {
        this.httpClient = HttpClient.newHttpClient();
        this.apiUrl = apiUrl;
    }

    @Override
    public List<String> detectLabels(byte[] imageBytes) {
        // Create a multipart/form-data body (standard for CodeProject.AI)
        // Note: For simplicity, you can use a library or a helper to build the multipart body
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "multipart/form-data; boundary=boundary")
                .POST(buildMultipartBody(imageBytes))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response.body());
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            log.error("Cannot detect labels due to an API error: {}", e.getMessage());
            throw new CannotDetectLabelsException("Failed to detect labels from the image", e);
        }
    }

    private HttpRequest.BodyPublisher buildMultipartBody(byte[] imageBytes) {

    }

    private List<String> parseResponse(HttpResponse<String> response) {

    }
}
