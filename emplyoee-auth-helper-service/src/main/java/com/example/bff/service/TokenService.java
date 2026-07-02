package com.example.bff.service;

public interface TokenService {
    /**
     * Store temporary values for the authorization request (state -> code_verifier)
     */
    void storePendingAuthorization(String state, String codeVerifier);

    /**
     * Exchange authorization code for tokens and persist them, returning a session id to set in cookie.
     */
    String exchangeCodeForToken(String state, String code);

    // ... other methods like refresh, revoke, getUserInfo can be added later
}
