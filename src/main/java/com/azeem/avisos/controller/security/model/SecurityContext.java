/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.model;

/**
 * Each thread gets its own isolated "currentUser"
 */
public class SecurityContext {
    private static final ThreadLocal<UserRecord> userThreadLocal =
            new InheritableThreadLocal<>(); // Null if guest

    public void setAuthenticatedUser(UserRecord user) {
        userThreadLocal.set(user); // Stores it in the current thread's "pocket"
    }

    public void clear() {
        userThreadLocal.remove(); // Essential to prevent memory leaks
    }

    public boolean isAuthenticated() {
        return userThreadLocal.get() != null;
    }

    public String getCurrentUsername() {
        UserRecord user = userThreadLocal.get();
        return user != null ? user.username() : "guest";
    }
}
