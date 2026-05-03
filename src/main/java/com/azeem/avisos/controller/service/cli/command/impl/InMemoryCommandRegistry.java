/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.service.cli.command.api.Command;
import com.azeem.avisos.controller.service.cli.command.api.CommandRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryCommandRegistry implements CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();

    @Override
    public void register(Command command) {
        commands.put(command.name().toLowerCase(), command);
    }

    @Override
    public Optional<Command> find(String name) {
        return Optional.ofNullable(commands.get(name.toLowerCase()));
    }
}
