/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli.command.impl;

import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.security.model.SecurityContext;
import com.azeem.avisos.controller.security.service.AuthService;
import com.azeem.avisos.controller.service.cli.command.api.Command;
import com.azeem.avisos.controller.security.model.UserRecord;

public class LoginCommand implements Command {
    private final CliClient cliClient;
    private final AuthService authService;
    private final SecurityContext securityContext;

    public LoginCommand(CliClient cliClient, AuthService authService, SecurityContext securityContext) {
        this.cliClient = cliClient;
        this.authService = authService;
        this.securityContext = securityContext;
    }

    @Override
    public String name() {
        return "login";
    }

    @Override
    public String description() {
        return """
                Begins the authentication handshake. Prompts the user for credentials to 
                establish a secure session context, enabling access to restricted operational 
                commands.
                """;
    }

    // TODO: debug and fix!
    @Override
    public void execute(String input) {
        String username = cliClient.readLn("Username: ");
        String password = cliClient.readLn("Password: ");

        if (authService.authenticate(username, password)) {
            // Fetch user data and update state
            UserRecord user = authService.getUser(username);
            securityContext.setAuthenticatedUser(user);
            cliClient.println("Login successful. Welcome, " + username);
        } else {
            cliClient.println("Invalid credentials.");
        }
    }
}
