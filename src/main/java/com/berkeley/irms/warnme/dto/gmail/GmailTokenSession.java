package com.berkeley.irms.warnme.dto.gmail;

import java.time.Instant;

public class GmailTokenSession {

    private final String accessToken;
    private final String refreshToken;
    private final Instant expiresAt;

    public GmailTokenSession(String accessToken, String refreshToken, Instant expiresAt) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpiredOrNearExpiry() {
        return expiresAt == null || Instant.now().isAfter(expiresAt.minusSeconds(60));
    }
}
