/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.service;

import com.azeem.avisos.controller.exceptions.UserDoesNotExistException;
import com.azeem.avisos.controller.repository.AuthRepository;
import com.azeem.avisos.controller.security.model.UserRecord;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthRepository authRepo;
    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public AuthService(AuthRepository authRepo) {
        this.authRepo = authRepo;
    }

    // Hash a plain password using Argon2id
    public String hashPassword(String password) {
        return argon2.hash(
                3,        // rounds (how many times the work is repeated)
                65536,    // memory in KB (64 MB)
                1,        // threads
                password
        );
    }

    // Login
    public boolean authenticate(String username, String password) {
        Optional<UserRecord> user = authRepo.findByUsername(username);
        if (user.isEmpty()) {
            throw new UserDoesNotExistException("The user by the username "
                    + username + " doesn't exist. Please try another username.");
        }
        return argon2.verify(user.get().passwordHash(), password);
    }

    // Register new user (only if user has not been created before)
    public void saveUser(String username, String password) {
        authRepo.createUser(username, hashPassword(password), "operator");
    }

    // Delete user (password required)
    public boolean removeUser(String username, String password) {
        Optional<UserRecord> user = authRepo.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserDoesNotExistException("Failed to remove user. The user by the username "
                    + username + " doesn't exist. Please try another username.");
        }

        if (!argon2.verify(user.get().passwordHash(), password)) {
            return false;
        }

        authRepo.deleteUser(username);
        log.info("Successfully removed user");
        return true;
    }

    // Change password (old password required)
    public boolean changeUserPassword(String username, String oldPass, String newPass) {
        Optional<UserRecord> user = authRepo.findByUsername(username);

        if (user.isEmpty()) {
            throw new UserDoesNotExistException("Failed to remove user. The user by the username "
                    + username + " doesn't exist. Please try another username.");
        }

        if (!argon2.verify(user.get().passwordHash(), oldPass)) {
            return false;
        }

        authRepo.updatePassword(username, hashPassword(newPass));
        log.info("Successfully changed password");
        return true;
    }

    public boolean hasAnyUsers() {
        return authRepo.countUsers() > 0;
    }

    public boolean userExists(String username) {
        return authRepo.findByUsername(username).isPresent();
    }
}
