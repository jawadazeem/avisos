/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.hardware;

import com.azeem.avisos.node.config.HardwareConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

/** Reads hardware vitals from the standalone C++ simulator REST API. */
public class HttpHardwareTelemetryProvider implements HardwareTelemetryProvider {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final URI readingsUri;
  private final Duration requestTimeout;

  public HttpHardwareTelemetryProvider(HardwareConfig config, ObjectMapper objectMapper) {
    this(
        HttpClient.newBuilder()
            .connectTimeout(Objects.requireNonNull(config, "config").requestTimeout())
            .build(),
        objectMapper,
        URI.create(config.simulatorBaseUrl()).resolve("/readings"),
        config.requestTimeout());
  }

  HttpHardwareTelemetryProvider(
      HttpClient httpClient, ObjectMapper objectMapper, URI readingsUri, Duration requestTimeout) {
    this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    this.readingsUri = Objects.requireNonNull(readingsUri, "readingsUri");
    this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
  }

  @Override
  public HardwareSnapshot readSnapshot() {
    HttpRequest request = HttpRequest.newBuilder(readingsUri).timeout(requestTimeout).GET().build();

    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new HardwareProviderException(
            "Hardware simulator returned status " + response.statusCode());
      }

      return objectMapper.readValue(response.body(), HardwareSnapshot.class);
    } catch (IOException e) {
      throw new HardwareProviderException("Failed to parse hardware simulator response", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new HardwareProviderException("Interrupted while reading hardware simulator", e);
    }
  }
}
