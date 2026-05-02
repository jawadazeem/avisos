/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.vision;

import com.azeem.avisos.controller.config.VisionConfig;
import com.azeem.avisos.controller.exceptions.CannotDetectLabelsException;
import com.azeem.avisos.controller.model.vision.VisionRequest;
import com.azeem.avisos.controller.model.vision.VisionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 *     Implementation of VisionClient that interacts with the CodeProject.AI vision API.
 *     It sends multipart/form-data requests containing the image and minimum confidence,
 *     and parses the JSON response to extract detected labels.
 * </p>
 * <p>
 *     Building a multipart/form-data by hand is largely unnecessary, I have done it here for
 *     learning purposes (RFC 7578).
 * </p>
 */
public class CodeProjectVisionClient implements VisionClient {
    private static final Logger log = LoggerFactory.getLogger(CodeProjectVisionClient.class);
    private final ObjectMapper jsonMapper;
    private final HttpClient httpClient;
    private final String apiUrl;

    public CodeProjectVisionClient(ObjectMapper jsonMapper,
                                   VisionConfig visionConfig
    ) {
        this.jsonMapper = jsonMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.apiUrl = visionConfig.apiUrl();
    }

    @Override
    public VisionResponse sendRequest(VisionRequest visionRequest) {
        String boundary = "AvisosBoundary" + UUID.randomUUID();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(buildMultipartBody(visionRequest, boundary))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            return parseResponse(response);
        } catch (IllegalArgumentException | IOException e) {
            log.error("Cannot detect labels from a request sent from device {} due to an " +
                    "API error: {}.", visionRequest.sensorId(), e.getMessage());
            throw new CannotDetectLabelsException("Failed to detect labels from the image.", e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new CannotDetectLabelsException("Thread was interrupted", ie);
        }
    }

    @Override
    public boolean isAvailable() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:32168/v1/module/status/vision"))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            log.warn("Vision AI Node is unreachable.");
            return false;
        }
    }

    /**
     * Creates a multipart/form-data body
     */
    private HttpRequest.BodyPublisher buildMultipartBody(VisionRequest visionRequest,
                                                         String boundary
    ) {
        List<byte[]> byteArrays = new ArrayList<>();

        // Image Part
        byteArrays.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays
                .add(("Content-Disposition: form-data; name=\"image\"; filename=\"upload.jpg\"\r\n")
                        .getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("Content-Type: image/jpeg\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(visionRequest.imageData());
        byteArrays.add(("\r\n").getBytes(StandardCharsets.UTF_8));

        // Confidence Part
        byteArrays.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("Content-Disposition: form-data; name=\"min_confidence\"\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        byteArrays.add((String.valueOf(visionRequest.minConfidence()))
                .getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("\r\n").getBytes(StandardCharsets.UTF_8));

        // Final Boundary
        byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return HttpRequest.BodyPublishers.ofByteArray(combineBytes(byteArrays));
    }

    /**
     * Parses the API response and extracts labels
     * @param response API response as a VisionResponse object. It should contain a list of labels
     *                 detected in the image.
     * @return List of detected labels
     */
    private VisionResponse parseResponse(HttpResponse<String> response) {
        String body = response.body();
        try {
            return jsonMapper.readValue(body, VisionResponse.class);
        } catch (IOException e) {
            log.error("Failed to parse vision API response: {}", e.getMessage());
            throw new CannotDetectLabelsException("Failed to parse vision API response", e);
        }
    }

    private static byte[] combineBytes(List<byte[]> byteArrays) {
        int totalLength = 0;
        for (byte[] b : byteArrays) totalLength += b.length;
        byte[] result = new byte[totalLength];
        int pos = 0;
        for (byte[] b : byteArrays) {
            System.arraycopy(b, 0, result, pos, b.length);
            pos += b.length;
        }
        return result;
    }
}
