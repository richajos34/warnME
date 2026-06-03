package com.berkeley.irms.warnme.controllers;

import com.berkeley.irms.warnme.dto.gmail.WarnMeEmailMetadata;
import com.berkeley.irms.warnme.services.GoogleOAuthService;
import com.berkeley.irms.warnme.services.WarnMeGmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.FOUND;

@RestController
@RequestMapping("/api/gmail")
public class GmailIngestionController {

    private final GoogleOAuthService googleOAuthService;
    private final WarnMeGmailService warnMeGmailService;

    public GmailIngestionController(
            GoogleOAuthService googleOAuthService,
            WarnMeGmailService warnMeGmailService) {
        this.googleOAuthService = googleOAuthService;
        this.warnMeGmailService = warnMeGmailService;
    }

    @GetMapping("/connect")
    public ResponseEntity<String> connect(
            @RequestParam(value = "confirm", defaultValue = "false") boolean confirm,
            HttpSession session) {
        if (!confirm) {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(buildConsentPage());
        }

        String authorizationUrl = googleOAuthService.createAuthorizationUrl(session);
        return ResponseEntity.status(FOUND)
                .header(HttpHeaders.LOCATION, authorizationUrl)
                .build();
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            HttpSession session) {
        if (error != null && !error.isBlank()) {
            return redirectToFrontend("gmail_error", error);
        }

        if (code == null || code.isBlank() || state == null || state.isBlank()) {
            return redirectToFrontend("gmail_error", "Missing Google OAuth callback parameters.");
        }

        try {
            googleOAuthService.handleCallback(code, state, session);
            return redirectToFrontend("gmail", "connected");
        } catch (ResponseStatusException exception) {
            return redirectToFrontend("gmail_error", exception.getReason());
        }
    }

    @GetMapping("/warnme/search")
    public Map<String, Object> searchWarnMeEmails(HttpSession session) {
        List<WarnMeEmailMetadata> messages = warnMeGmailService.searchWarnMeEmails(session);
        return Map.of(
                "connected", googleOAuthService.hasConnectedGmail(session),
                "query", WarnMeGmailService.WARNME_GMAIL_QUERY,
                "count", messages.size(),
                "messages", messages);
    }

    private ResponseEntity<Void> redirectToFrontend(String key, String value) {
        URI redirectUri = URI.create(UriComponentsBuilder.fromPath("/map.html")
                .queryParam(key, value == null ? "" : value)
                .fragment("gmail-ingestion")
                .build()
                .toUriString());

        return ResponseEntity.status(FOUND)
                .location(redirectUri)
                .build();
    }

    private String buildConsentPage() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Connect Berkeley Gmail | SafeZone</title>
                  <style>
                    body { font-family: system-ui, sans-serif; max-width: 760px; margin: 40px auto; padding: 0 20px; line-height: 1.55; color: #0f172a; }
                    .panel { border: 1px solid #dbe3ee; border-radius: 18px; padding: 28px; background: #fff; }
                    h1 { line-height: 1.1; }
                    .actions { display: flex; gap: 12px; flex-wrap: wrap; margin-top: 24px; }
                    a { display: inline-flex; min-height: 44px; align-items: center; justify-content: center; border-radius: 10px; padding: 0 16px; text-decoration: none; font-weight: 700; }
                    .primary { background: #2563eb; color: #fff; }
                    .secondary { border: 1px solid #cbd5e1; color: #0f172a; }
                  </style>
                </head>
                <body>
                  <main class="panel">
                    <p>SafeZone Gmail ingestion</p>
                    <h1>Connect Berkeley Gmail</h1>
                    <p>SafeZone will ask Google for read-only Gmail permission. Do not enter your Gmail password into SafeZone.</p>
                    <h2>SafeZone will</h2>
                    <ul>
                      <li>Search your Gmail for UC Berkeley WarnMe emergency notifications.</li>
                      <li>Only look for emails from UC Berkeley WarnMe Emergency Notification ucberkeley@warnme.berkeley.edu.</li>
                      <li>Only look for emails with subjects beginning with UC Berkeley WarnMe.</li>
                      <li>Only search emails from the past year.</li>
                      <li>Use the results to build a private incident history for SafeZone.</li>
                    </ul>
                    <h2>SafeZone will not</h2>
                    <ul>
                      <li>Send emails.</li>
                      <li>Delete emails.</li>
                      <li>Modify emails.</li>
                      <li>Read unrelated email content intentionally.</li>
                      <li>Store your Gmail password.</li>
                    </ul>
                    <div class="actions">
                      <a class="primary" href="/api/gmail/connect?confirm=true">Continue to Google OAuth</a>
                      <a class="secondary" href="/map.html#gmail-ingestion">Cancel</a>
                    </div>
                  </main>
                </body>
                </html>
                """;
    }
}
