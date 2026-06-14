/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.exceptions;

public class CannotLoadKnowledgeFilesExceptions extends RuntimeException {
  public CannotLoadKnowledgeFilesExceptions(String message) {
    super(message);
  }

  public CannotLoadKnowledgeFilesExceptions(String message, Throwable cause) {
    super(message, cause);
  }
}
