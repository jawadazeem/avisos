/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.api;

import java.util.Optional;

public interface CommandRegistry {
    void register(Command command);
    Optional<Command> find(String name);
}
