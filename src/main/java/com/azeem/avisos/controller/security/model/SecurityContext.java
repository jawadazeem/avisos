/*
 * (C) Copyright 2026 Jawad Azeem
 * Apache 2.0 License
 */

package com.azeem.avisos.controller.security.model;

public class SecurityContext {
    private UserRecord currentUser; // Null if guest

    public void setAuthenticatedUser(UserRecord user) {
        this.currentUser = user;
    }

    public void clear() {
        this.currentUser = null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.username() : "guest";
    }
}
