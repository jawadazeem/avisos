/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.infrastructure.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BufferingCliClientTest {

  private BufferingCliClient client;

  @BeforeEach
  void setUp() {
    client = new BufferingCliClient();
  }

  @Test
  void println_shouldCaptureOutputWithNewline() {
    client.println("hello");

    assertEquals("hello\n", client.getOutput());
  }

  @Test
  void println_shouldAccumulateMultipleLines() {
    client.println("line 1");
    client.println("line 2");
    client.println("line 3");

    assertEquals("line 1\nline 2\nline 3\n", client.getOutput());
  }

  @Test
  void getOutput_shouldReturnEmptyStringWhenNothingWritten() {
    assertEquals("", client.getOutput());
  }

  @Test
  void isRunning_shouldAlwaysReturnTrue() {
    assertTrue(client.isRunning());
  }

  @Test
  void readLn_shouldThrowUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class, () -> client.readLn());
  }

  @Test
  void readLnWithPrompt_shouldThrowUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class, () -> client.readLn("prompt> "));
  }

  @Test
  void readPassword_shouldThrowUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class, () -> client.readPassword());
  }

  @Test
  void readPasswordWithPrompt_shouldThrowUnsupportedOperationException() {
    assertThrows(UnsupportedOperationException.class, () -> client.readPassword("password: "));
  }

  @Test
  void initAndShutdown_shouldNotThrow() {
    assertDoesNotThrow(() -> client.init());
    assertDoesNotThrow(() -> client.shutdown());
  }
}
