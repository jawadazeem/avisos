/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.service;

import com.azeem.avisos.controller.exceptions.UserDoesNotExistException;
import com.azeem.avisos.controller.security.repository.AuthRepository;
import com.azeem.avisos.controller.security.model.UserRecord;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Service class responsible for handling user authentication, password hashing, and user management.
 * It uses the Argon2 algorithm for secure password hashing and interacts with the AuthRepository for data persistence.
 */
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthRepository authRepo;
    private final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

    public AuthService(AuthRepository authRepo) {
        this.authRepo = authRepo;
    }

    /**
     * Hash the password using Argon2 algorithm with specified parameters.
     * @return the hashed password
     */
    public String hashPassword(String password) {
        return argon2.hash(
                3,        // rounds (how many times the work is repeated)
                65536,    // memory in KB (64 MB)
                1,        // threads
                password
        );
    }

    /**
     * Authenticate user by verifying the provided password against the stored hash.
     * @return true if authentication is successful, false if password verification fails, throws exception if user doesn't exist
     */
    public boolean authenticate(String username, String password) {
        Optional<UserRecord> user = authRepo.findByUsername(username);
        if (user.isEmpty()) {
            throw new UserDoesNotExistException("The user by the username "
                    + username + " doesn't exist. Please try another username.");
        }
        return argon2.verify(user.get().passwordHash(), password);
    }

    /**
     * Save a new user with the provided username and password. The password will be hashed before storing.
     */
    public void saveUser(String username, String password) {
        authRepo.createUser(username, hashPassword(password), "operator");
    }

    /**
     * Remove user (only if old password is provided correctly)
     * @return true if user was removed successfully, false if password verification failed
     */
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

    /**
     * Change user password (only if old password is provided correctly)
     * @return true if password was changed successfully, false if password verification failed
     */
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

    /**
     * Check if there are any users in the system. This can be used to determine if the initial setup is complete.
     * @return true if at least one user exists, false otherwise
     */
    public boolean hasAnyUsers() {
        return authRepo.countUsers() > 0;
    }

    /**
     * Check if a user with the given username exists in the system.
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username) {
        return authRepo.findByUsername(username).isPresent();
    }
}
