package com.safeNotes.services.auth;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.safeNotes.models.domain.User;

public class SessionManager {

    private final Map<String, User> sessions = new ConcurrentHashMap<>();

    public String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, user);
        return sessionId;
    }

    public void logout() {
        sessions.clear();
    }
}
