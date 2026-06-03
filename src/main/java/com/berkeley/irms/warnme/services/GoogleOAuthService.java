package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.config.GoogleOAuthProperties;
import com.berkeley.irms.warnme.dto.gmail.GmailTokenSession;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class GoogleOAuthService {

    public static final String GMAIL_TOKEN_SESSION_KEY = "safezone.gmail.tokens";
    private static final String GMAIL_OAUTH_STATE_KEY = "safezone.gmail.oauth.state";
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final GoogleOAuthProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleOAuthService(GoogleOAuthProperties properties) {
        this.properties = properties;
    }

    public String createAuthorizationUrl(HttpSession session) {
        assertConfigured();

        String state = UUID.randomUUID().toString();
        session.setAttribute(GMAIL_OAUTH_STATE_KEY, state);

        return UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", GoogleOAuthProperties.GMAIL_READONLY_SCOPE)
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("include_granted_scopes", "true")
                .build()
                .encode()
                .toUriString();
    }

    public void handleCallback(String code, String state, HttpSession session) {
        assertConfigured();

        Object expectedState = session.getAttribute(GMAIL_OAUTH_STATE_KEY);
        session.removeAttribute(GMAIL_OAUTH_STATE_KEY);

        if (expectedState == null || !expectedState.equals(state)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid Google OAuth state.");
        }

        GmailTokenSession tokenSession = exchangeCodeForTokens(code);

        // TODO: Replace development-only HTTP session token storage with encrypted,
        // per-user persistence once SafeZone has a real authentication/session system.
        session.setAttribute(GMAIL_TOKEN_SESSION_KEY, tokenSession);
    }

    public GmailTokenSession getUsableTokenSession(HttpSession session) {
        Object tokenAttribute = session.getAttribute(GMAIL_TOKEN_SESSION_KEY);
        if (!(tokenAttribute instanceof GmailTokenSession tokenSession)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Connect Berkeley Gmail before searching WarnMe emails.");
        }

        if (!tokenSession.isExpiredOrNearExpiry()) {
            return tokenSession;
        }

        if (tokenSession.getRefreshToken() == null || tokenSession.getRefreshToken().isBlank()) {
            session.removeAttribute(GMAIL_TOKEN_SESSION_KEY);
            throw new ResponseStatusException(UNAUTHORIZED, "Gmail access expired. Reconnect Berkeley Gmail.");
        }

        GmailTokenSession refreshedSession = refreshAccessToken(tokenSession.getRefreshToken());
        session.setAttribute(GMAIL_TOKEN_SESSION_KEY, refreshedSession);
        return refreshedSession;
    }

    public boolean hasConnectedGmail(HttpSession session) {
        return session.getAttribute(GMAIL_TOKEN_SESSION_KEY) instanceof GmailTokenSession;
    }

    private GmailTokenSession exchangeCodeForTokens(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("code", code);
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("redirect_uri", properties.getRedirectUri());
        form.add("grant_type", "authorization_code");

        JsonNode response = postTokenRequest(form);
        String accessToken = response.path("access_token").asText("");
        String refreshToken = response.path("refresh_token").asText(null);
        long expiresIn = response.path("expires_in").asLong(3600L);

        if (accessToken.isBlank()) {
            throw new ResponseStatusException(BAD_GATEWAY, "Google OAuth did not return an access token.");
        }

        return new GmailTokenSession(accessToken, refreshToken, Instant.now().plusSeconds(expiresIn));
    }

    private GmailTokenSession refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        form.add("refresh_token", refreshToken);
        form.add("grant_type", "refresh_token");

        JsonNode response = postTokenRequest(form);
        String accessToken = response.path("access_token").asText("");
        long expiresIn = response.path("expires_in").asLong(3600L);

        if (accessToken.isBlank()) {
            throw new ResponseStatusException(BAD_GATEWAY, "Google OAuth refresh did not return an access token.");
        }

        return new GmailTokenSession(accessToken, refreshToken, Instant.now().plusSeconds(expiresIn));
    }

    private JsonNode postTokenRequest(MultiValueMap<String, String> form) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            JsonNode response = restTemplate.postForObject(GOOGLE_TOKEN_URL, new HttpEntity<>(form, headers), JsonNode.class);
            if (response == null) {
                throw new ResponseStatusException(BAD_GATEWAY, "Google OAuth returned an empty token response.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Google OAuth token exchange failed.", exception);
        }
    }

    private void assertConfigured() {
        if (!properties.isConfigured()) {
            throw new ResponseStatusException(
                    PRECONDITION_FAILED,
                    "Google OAuth is not configured. Set GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, and GOOGLE_REDIRECT_URI.");
        }
    }
}
