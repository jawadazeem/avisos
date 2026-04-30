/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.exceptions;

public class CannotDetectLabelsException extends RuntimeException {
    public CannotDetectLabelsException(String message) {
        super(message);
    }

    public CannotDetectLabelsException(String message, Throwable cause) {
        super(message, cause);
    }
}
