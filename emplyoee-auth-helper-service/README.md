# BFF OAuth2 Skeleton (Gradle)

This is a minimal Backend-for-Frontend (BFF) skeleton for handling OAuth2 for a SPA.

Quick start (requires Gradle):

```powershell
# From project root
gradle clean build
gradle bootRun
```

Environment variables (examples):
- OAUTH_CLIENT_ID
- OAUTH_CLIENT_SECRET
- OAUTH_PROVIDER_AUTH_URI
- OAUTH_PROVIDER_TOKEN_URI
- OAUTH_REDIRECT_URI (defaults to http://localhost:8080/auth/callback)
- FRONTEND_ORIGIN (defaults to http://localhost:3000)

Endpoints:
- GET /auth/login -> returns { url }
- GET /auth/callback -> exchange code and sets HttpOnly session cookie, redirects to FRONTEND_ORIGIN
- (other endpoints to be added: /auth/refresh, /auth/me, /auth/logout)

This skeleton uses an in-memory token store. For production, wire a JDBC or Redis-backed store and implement token exchange with the provider's token endpoint.
