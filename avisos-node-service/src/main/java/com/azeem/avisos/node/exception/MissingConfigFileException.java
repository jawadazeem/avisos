/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.node.exception;

public class MissingConfigFileException extends RuntimeException {
    public MissingConfigFileException(String message) {
        super(message);
    }
}
