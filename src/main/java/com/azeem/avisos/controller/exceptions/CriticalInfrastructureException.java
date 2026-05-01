package com.azeem.avisos.controller.exceptions;

public class CriticalInfrastructureException extends RuntimeException {
  public CriticalInfrastructureException(String message) {
    super(message);
  }
}
