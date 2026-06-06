/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.web;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  void handleBadRequest_shouldReturn400WithMessage() {
    IllegalArgumentException ex = new IllegalArgumentException("Invalid node ID");

    ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid node ID", response.getBody().get("error"));
    assertEquals(400, response.getBody().get("status"));
  }

  @Test
  void handleGeneral_shouldReturn500WithGenericMessage() {
    RuntimeException ex = new RuntimeException("Something broke");

    ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals("Internal server error", response.getBody().get("error"));
    assertEquals(500, response.getBody().get("status"));
  }

  @Test
  void handleNotFound_shouldReturn404ForMissingStaticResources() {
    NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/favicon.ico");

    ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals("Resource not found", response.getBody().get("error"));
    assertEquals(404, response.getBody().get("status"));
  }

  @Test
  void handleGeneral_shouldNotLeakExceptionDetails() {
    RuntimeException ex = new RuntimeException("SQL injection attempt detected");

    ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

    assertFalse(response.getBody().get("error").toString().contains("SQL"));
    assertEquals("Internal server error", response.getBody().get("error"));
  }
}
