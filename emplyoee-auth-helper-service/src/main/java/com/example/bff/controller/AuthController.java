package com.example.bff.controller;

import com.example.bff.service.TokenService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenService tokenService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Value("${app.oauth.client-id:}")
    private String clientId;

    @Value("${app.oauth.authorization-uri:http://localhost:9000/oauth/authorize}")
    private String authorizationUri;

    @Value("${app.oauth.redirect-uri:http://localhost:8081/auth/callback}")
    private String redirectUri;

    @Value("${app.frontend-origin:http://localhost:3000}")
    private String frontendOrigin;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostConstruct
    public void validateConfig() {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("app.oauth.client-id must be set");
        }
        if (authorizationUri == null || authorizationUri.isBlank()) {
            throw new IllegalStateException("app.oauth.authorization-uri must be set");
        }
        // Log resolved values to help debugging (e.g. which redirect URI and server port are in use)
        log.info("OAuth clientId={}, authorizationUri={}, redirectUri={}", clientId, authorizationUri, redirectUri);
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> login() throws Exception {
        // generate high-entropy, URL-safe state and code_verifier
        SecureRandom rng = new SecureRandom();
        byte[] stateBytes = new byte[32];
        rng.nextBytes(stateBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);

        byte[] verifierBytes = new byte[64];
        rng.nextBytes(verifierBytes);
        String codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes);
        // compute code challenge
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashed = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        String codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);

        tokenService.storePendingAuthorization(state, codeVerifier);
        // URL-encode individual query parameter values
        String encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8.toString());
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString());
        String encodedScope = URLEncoder.encode("openid profile email offline_access", StandardCharsets.UTF_8.toString());
        String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8.toString());
        String encodedChallenge = URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8.toString());

        String url = authorizationUri
                + "?response_type=code&client_id=" + encodedClientId
                + "&redirect_uri=" + encodedRedirect
                + "&scope=" + encodedScope
                + "&state=" + encodedState
                + "&code_challenge=" + encodedChallenge
                + "&code_challenge_method=S256";

        // log the outgoing authorization URL (safe to log; doesn't contain client_secret)
        log.debug("Authorization URL: {}", url);

        // Redirect the user's browser directly to the authorization endpoint (recommended for BFF)
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam(name = "code", required = false) String code,
                                         @RequestParam(name = "state", required = false) String state,
                                         HttpSession session) {
        if (code == null || state == null) {
            return ResponseEntity.badRequest().build();
        }

        String sessionId = tokenService.exchangeCodeForToken(state, code);

        // Store session attributes in HttpSession - this triggers Spring Session to write to Redis
        session.setAttribute("sessionId", sessionId);
        session.setAttribute("userId", "user-" + code); // example: store user info
        session.setAttribute("isAuthenticated", true);

        log.info("Session created and stored in Redis. SessionId={}", session.getId());

        // Spring Session CookieSerializer will automatically handle the Set-Cookie header
        return ResponseEntity.status(302).location(URI.create(frontendOrigin)).build();
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> session(HttpSession session) {
        Boolean isAuthenticated = (Boolean) session.getAttribute("isAuthenticated");
        String userId = (String) session.getAttribute("userId");

        return ResponseEntity.ok(Map.of(
                "authenticated", Boolean.TRUE.equals(isAuthenticated),
                "userId", userId == null ? "Guest" : userId
        ));
    }

    /**
     * Test endpoint: Store a test value in session and verify it appears in Redis
     * Call this to test Redis session persistence
     */
    @GetMapping("/test-session")
    public ResponseEntity<Map<String, String>> testSession(HttpSession session) {
        // Store test data
        session.setAttribute("testKey", "testValue_" + System.currentTimeMillis());
        session.setAttribute("timestamp", String.valueOf(System.currentTimeMillis()));

        String sessionId = session.getId();
        log.info("Test session created: {}", sessionId);

        return ResponseEntity.ok(Map.of(
                "message", "Session data stored",
                "sessionId", sessionId,
                "instructions", "Run: docker exec -it local-redis redis-cli KEYS 'spring:session:*' and HGETALL 'spring:session:sessions:" + sessionId + "'"
        ));
    }
}
