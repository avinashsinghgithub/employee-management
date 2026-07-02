package com.example.bff.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
public class InMemoryTokenStore implements TokenService {

    private final Map<String, String> pending = new ConcurrentHashMap<>();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    @Override
    public void storePendingAuthorization(String state, String codeVerifier) {
        pending.put(state, codeVerifier);
    }

    @Override
    public String exchangeCodeForToken(String state, String code) {
        String verifier = pending.remove(state);
        if (verifier == null) {
            throw new IllegalArgumentException("Invalid or expired state");
        }
        // In a real implementation we'd call the token endpoint here, exchange code+verifier -> tokens
        // For this skeleton we create a fake session id and store the code as proof
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, code);
        return sessionId;
    }

    // helper for tests
    public boolean hasSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}
