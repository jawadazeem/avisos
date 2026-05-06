/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.security.model.SecurityContext;
import com.azeem.avisos.controller.security.service.AuthService;
import com.azeem.avisos.controller.service.cli.command.api.Command;

public class SecureCommand implements Command {
    private final CliClient cliClient;
    private final Command delegate;
    private final SecurityContext context;
    private final AuthService authService;

    public SecureCommand(CliClient cliClient, Command delegate, AuthService authService, SecurityContext context) {
        this.cliClient = cliClient;
        this.delegate = delegate;
        this.authService = authService;
        this.context = context;
    }

    @Override
    public String name() {
        return "secure";
    }

    @Override
    public String description() {
        return """
                A meta-command (decorator) that wraps sensitive operations. It validates the 
                current session's claims against the required security policy before delegating 
                execution to the underlying command.
                """;
    }

    public void execute(String input) {
        if (context.isAuthenticated()) {
            delegate.execute(input);
        } else {
            cliClient.println("Access Denied: Please login first.");
        }
    }
}
