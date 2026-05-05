/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.service.cli;

import com.azeem.avisos.controller.exceptions.UserDoesNotExistException;
import com.azeem.avisos.controller.infrastructure.cli.CliClient;
import com.azeem.avisos.controller.security.model.SecurityContext;
import com.azeem.avisos.controller.security.model.UserRecord;
import com.azeem.avisos.controller.security.service.AuthService;
import com.azeem.avisos.controller.service.cli.command.api.CommandRegistry;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JLineCliService implements CliService {
    private static final Logger log = LoggerFactory.getLogger(JLineCliService.class);
    private final CliClient cliClient;
    private final CommandRegistry commandRegistry;
    private final AuthService authService;
    private final SecurityContext securityContext;
    private final ExecutorService executor;

    public JLineCliService(CliClient cliClient,
                           CommandRegistry commandRegistry,
                           AuthService authService,
                           SecurityContext securityContext
    ) {
        this.cliClient = cliClient;
        this.commandRegistry = commandRegistry;
        this.authService = authService;
        this.securityContext = securityContext;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void start() {
        try {
            // Gateway: Block until we have a user
            performAuthenticationBlocking();

            // Phase 2: Operational Loop
            enterCommandLoop();
        } catch (Exception e) {
            log.error("Fatal system error in CLI supervisor", e);
        }
    }

    private void performAuthenticationBlocking() {
        cliClient.init();
        bootstrapCheck();

        while (cliClient.isRunning() && !securityContext.isAuthenticated()) {
            try {
                String username = cliClient.readLn("Username: ");
                String password = cliClient.readPassword(); // Masked

                if (authService.authenticate(username, password)) {
                    Optional<UserRecord> user = authService.getUser(username);
                    if (user.isEmpty()) {
                        continue;
                    }
                    securityContext.setAuthenticatedUser(user.get());
                    cliClient.println("Login successful.");
                } else {
                    cliClient.println("Error: Invalid password.");
                }
            } catch (UserDoesNotExistException e) {
                log.warn("Auth failure: no such user exists");
                cliClient.println("Error: " + e.getMessage());
            } catch (IllegalStateException | UserInterruptException | EndOfFileException e) {
                shutdownCli();
            } catch (Exception e) {
                log.error("Critical Auth Error");
                cliClient.println("A system error occurred. Please contact an administrator.");
            }
        }
    }

    private void enterCommandLoop() {
        while (cliClient.isRunning()) {
            try {
                String input = cliClient.readLn().trim();
                commandRegistry.find(input)
                        .ifPresentOrElse(
                                cmd -> executor.submit(() -> cmd.execute(input)),
                                () -> cliClient.println("Unknown command: " + input)
                        );
            } catch (IllegalStateException | UserInterruptException | EndOfFileException e) {
                log.info("CLI Session closed by user. Background services remain active.");
                shutdownCli();
            }
        }
    }

    private void bootstrapCheck() {
        if (!authService.hasAnyUsers()) {
            cliClient.println("There are no users in this system.");
            cliClient.println("Initializing AVISOS Administrative Bootstrap...");

            String username = cliClient.readLn("Create Admin Username: ");
            String password = cliClient.readPassword("Create Admin Password: ");

            authService.saveUser(username, password);
            cliClient.println("Admin account created successfully. Proceeding to login...");
        }
    }

    /**
     * Only shuts down the CLI, not the entire application.
     */
    private void shutdownCli() {
        cliClient.shutdown();
        executor.shutdown();
    }
}
