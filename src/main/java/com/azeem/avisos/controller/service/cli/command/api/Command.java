/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.api;

public interface Command {
    String name();
    void execute(String input);
    String description();

    default boolean matches(String input) {
        return input.trim().toLowerCase().startsWith(name().toLowerCase());
    }
}
