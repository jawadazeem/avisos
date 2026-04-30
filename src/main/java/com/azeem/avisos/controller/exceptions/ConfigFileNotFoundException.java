/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.exceptions;

public class ConfigFileNotFoundException extends RuntimeException {
    public ConfigFileNotFoundException(String message) {
        super(message);
    }

    public ConfigFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
