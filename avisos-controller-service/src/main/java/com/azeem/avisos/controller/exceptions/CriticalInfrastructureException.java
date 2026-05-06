/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.exceptions;

public class CriticalInfrastructureException extends RuntimeException {
    public CriticalInfrastructureException(String message) {
        super(message);
    }

    public CriticalInfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
