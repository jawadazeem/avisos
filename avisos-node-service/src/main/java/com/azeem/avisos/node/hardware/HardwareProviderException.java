/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.hardware;

/** Raised when the node cannot read hardware telemetry from the configured provider. */
public class HardwareProviderException extends RuntimeException {
  public HardwareProviderException(String message) {
    super(message);
  }

  public HardwareProviderException(String message, Throwable cause) {
    super(message, cause);
  }
}
