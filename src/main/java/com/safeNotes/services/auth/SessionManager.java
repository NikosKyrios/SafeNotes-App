package com.safeNotes.services.auth;


import java.util.UUID;

import com.safeNotes.models.domain.User;

public class SessionManager {

    private static SessionManager instance;
    private User currentUser;
    private boolean isLoggedIn;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        this.isLoggedIn = (user != null);
    }

    public User getCurrentUser() {
        return currentUser;
    }
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : null;
    }

    public void clearSession() {
        if (currentUser != null) {
            currentUser.clearSensitiveData();
        }
        this.currentUser = null;
        this.isLoggedIn = false;
    }

    public String createSession(User user) {
        this.currentUser = user;
        this.isLoggedIn = true;
        return UUID.randomUUID().toString();
    }

    public void logout() {
        clearSession();
    }
}
