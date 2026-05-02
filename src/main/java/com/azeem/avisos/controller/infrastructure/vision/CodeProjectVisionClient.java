/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.vision;

import com.azeem.avisos.controller.config.MqttConfig;
import com.azeem.avisos.controller.config.VisionConfig;
import com.azeem.avisos.controller.exceptions.CannotDetectLabelsException;
import com.azeem.avisos.controller.exceptions.ConfigFileMisconfiguredException;
import com.azeem.avisos.controller.exceptions.ConfigFileNotFoundException;
import com.azeem.avisos.controller.exceptions.CriticalInfrastructureException;
import com.azeem.avisos.controller.model.vision.VisionRequest;
import com.azeem.avisos.controller.model.vision.VisionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CodeProjectVisionClient implements VisionClient {
    private static final Logger log = LoggerFactory.getLogger(CodeProjectVisionClient.class);
    private final ObjectMapper ymlMapper;
    private final ObjectMapper jsonMapper;
    private final HttpClient httpClient;
    private final String apiUrl;

    public CodeProjectVisionClient(ObjectMapper ymlMapper, ObjectMapper jsonMapper) {
        this.ymlMapper = ymlMapper;
        this.jsonMapper = jsonMapper;
        this.httpClient = HttpClient.newHttpClient();
        VisionConfig visionConfig = loadConfig();
        this.apiUrl = visionConfig.apiUrl();
    }

    @Override
    public VisionResponse sendRequest(VisionRequest visionRequest) {

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "multipart/form-data; boundary=boundary")
                    .POST(buildMultipartBody(visionRequest))
                    .build();
        } catch (UnsupportedEncodingException e) {
            log.error("Failed to build multipart body for vision request: {}", e.getMessage());
        }

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            return parseResponse(response);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            log.error("Cannot detect labels from a request sent from device {} due to an " +
                    "API error: {}.", visionRequest.sensorId(), e.getMessage());
            throw new CannotDetectLabelsException("Failed to detect labels from the image.", e);
        }
    }

    @Override
    public boolean isAvailable() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            return response.statusCode() == 200;
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            log.error("Vision API health check failed: {}", e.getMessage());
            throw new CannotDetectLabelsException("Failed to connect to vision API for health check.", e);
        }
    }

    /**
     * Creates a multipart/form-data body
     */
    private HttpRequest.BodyPublisher buildMultipartBody(VisionRequest visionRequest)
            throws UnsupportedEncodingException {
        String boundary = "Boundary" + UUID.randomUUID().toString();

        List<byte[]> byteArrays = new ArrayList<>();
        byteArrays.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays
                .add(("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("Content-Type: image/jpeg").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(visionRequest.imageData());

        byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        byteArrays.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        byteArrays.add(("Content-Disposition: form-data; name=\"min_confidence\"\r\n\r\n")
                        .getBytes(StandardCharsets.UTF_8));
        byteArrays.add((String.valueOf(visionRequest.minConfidence()))
                .getBytes(StandardCharsets.UTF_8));

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

    private VisionConfig loadConfig() {
        try (InputStream is = getClass().getResourceAsStream("/application.yml")) {
            if (is == null) {
                throw new CriticalInfrastructureException(
                        "CRITICAL: Config file not found in classpath!"
                );
            }
            return ymlMapper.readValue(is, VisionConfig.class);
        } catch (IOException e) {
            throw new ConfigFileMisconfiguredException("Failed to parse security policy", e);
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
