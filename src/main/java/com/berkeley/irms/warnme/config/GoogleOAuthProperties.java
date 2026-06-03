package com.berkeley.irms.warnme.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GoogleOAuthProperties {

    public static final String GMAIL_READONLY_SCOPE = "https://www.googleapis.com/auth/gmail.readonly";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOAuthProperties(
            @Value("${google.oauth.client-id:}") String clientId,
            @Value("${google.oauth.client-secret:}") String clientSecret,
            @Value("${google.oauth.redirect-uri:}") String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(clientId)
                && StringUtils.hasText(clientSecret)
                && StringUtils.hasText(redirectUri);
    }
}
