/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.exceptions;

public class CannotFindKnowledgeFileException extends RuntimeException {
  public CannotFindKnowledgeFileException(String message) {
    super(message);
  }

  public CannotFindKnowledgeFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
