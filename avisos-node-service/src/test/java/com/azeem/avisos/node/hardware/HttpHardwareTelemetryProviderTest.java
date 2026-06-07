/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Test;

class HttpHardwareTelemetryProviderTest {
  @Test
  void readSnapshot_shouldParseValidReadingsResponse() {
    HttpHardwareTelemetryProvider provider =
        provider(
            new FakeHttpClient(
                200,
                """
                {
                  "batteryPercent": 64,
                  "temperatureCelsius": 7.5,
                  "pressureKpa": 235.8,
                  "leakDetected": true,
                  "humidityPercent": 91.2,
                  "signalQualityPercent": 78,
                  "timestamp": "2026-06-06T02:00:00Z"
                }
                """,
                null));

    HardwareSnapshot snapshot = provider.readSnapshot();

    assertEquals(64, snapshot.batteryPercent());
    assertEquals(7.5, snapshot.temperatureCelsius());
    assertEquals(235.8, snapshot.pressureKpa());
    assertEquals(true, snapshot.leakDetected());
    assertEquals(91.2, snapshot.humidityPercent());
    assertEquals(78, snapshot.signalQualityPercent());
  }

  @Test
  void readSnapshot_shouldThrowOnNonSuccessStatus() {
    HttpHardwareTelemetryProvider provider =
        provider(new FakeHttpClient(503, "{\"error\":\"simulator warming up\"}", null));

    assertThrows(HardwareProviderException.class, provider::readSnapshot);
  }

  @Test
  void readSnapshot_shouldThrowOnMalformedJson() {
    HttpHardwareTelemetryProvider provider =
        provider(new FakeHttpClient(200, "{\"batteryPercent\":\"not-a-number\"}", null));

    assertThrows(HardwareProviderException.class, provider::readSnapshot);
  }

  @Test
  void readSnapshot_shouldThrowOnTimeout() {
    HttpHardwareTelemetryProvider provider =
        provider(
            new FakeHttpClient(
                200, "{\"batteryPercent\":50}", new HttpTimeoutException("timed out")));

    assertThrows(HardwareProviderException.class, provider::readSnapshot);
  }

  private HttpHardwareTelemetryProvider provider(HttpClient httpClient) {
    return new HttpHardwareTelemetryProvider(
        httpClient,
        new ObjectMapper().registerModule(new JavaTimeModule()),
        URI.create("http://hardware-simulator:5000/readings"),
        Duration.ofSeconds(1));
  }

  private static final class FakeHttpClient extends HttpClient {
    private final int status;
    private final String body;
    private final IOException failure;

    private FakeHttpClient(int status, String body, IOException failure) {
      this.status = status;
      this.body = body;
      this.failure = failure;
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
      return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
      return Optional.of(Duration.ofSeconds(1));
    }

    @Override
    public Redirect followRedirects() {
      return Redirect.NEVER;
    }

    @Override
    public Optional<ProxySelector> proxy() {
      return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
      return null;
    }

    @Override
    public SSLParameters sslParameters() {
      return null;
    }

    @Override
    public Optional<Authenticator> authenticator() {
      return Optional.empty();
    }

    @Override
    public Version version() {
      return Version.HTTP_1_1;
    }

    @Override
    public Optional<Executor> executor() {
      return Optional.empty();
    }

    @Override
    public <T> HttpResponse<T> send(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException {
      if (failure != null) {
        throw failure;
      }

      @SuppressWarnings("unchecked")
      T typedBody = (T) body;
      return new FakeHttpResponse<>(status, typedBody, request);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
      throw new UnsupportedOperationException("sendAsync is not used by this provider");
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
        HttpRequest request,
        HttpResponse.BodyHandler<T> responseBodyHandler,
        HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
      throw new UnsupportedOperationException("sendAsync is not used by this provider");
    }
  }

  private record FakeHttpResponse<T>(int statusCode, T body, HttpRequest request)
      implements HttpResponse<T> {
    @Override
    public Optional<HttpResponse<T>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
      return HttpHeaders.of(Map.of(), (name, value) -> true);
    }

    @Override
    public Optional<SSLSession> sslSession() {
      return Optional.empty();
    }

    @Override
    public URI uri() {
      return request.uri();
    }

    @Override
    public HttpClient.Version version() {
      return HttpClient.Version.HTTP_1_1;
    }
  }
}
