/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.exceptions;

public class ConfigFileMisconfiguredException extends RuntimeException {
    public ConfigFileMisconfiguredException(String message) {
        super(message);
    }

    public ConfigFileMisconfiguredException(String message, Throwable cause) {
        super(message, cause);
    }
}
