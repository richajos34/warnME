package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.dto.gmail.GmailTokenSession;
import com.berkeley.irms.warnme.dto.gmail.ParsedWarnMeEmail;
import com.berkeley.irms.warnme.dto.gmail.RawWarnMeEmail;
import com.berkeley.irms.warnme.dto.gmail.WarnMeEmailMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

@Service
public class WarnMeGmailService {

    public static final String WARNME_SENDER_EMAIL = "ucberkeley@warnme.berkeley.edu";
    public static final String WARNME_SUBJECT_PREFIX = "UC Berkeley WarnMe";
    public static final String WARNME_GMAIL_QUERY =
            "from:" + WARNME_SENDER_EMAIL + " newer_than:1y";

    private static final String GMAIL_MESSAGES_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages";
    private static final int MAX_CANDIDATE_MESSAGES = 100;

    private final GoogleOAuthService googleOAuthService;
    private final RestTemplate restTemplate = new RestTemplate();

    public WarnMeGmailService(GoogleOAuthService googleOAuthService) {
        this.googleOAuthService = googleOAuthService;
    }

    public List<WarnMeEmailMetadata> searchWarnMeEmails(HttpSession session) {
        GmailTokenSession tokenSession = googleOAuthService.getUsableTokenSession(session);
        List<JsonNode> candidateMessages = listCandidateMessages(tokenSession.getAccessToken());
        List<WarnMeEmailMetadata> matches = new ArrayList<>();

        for (JsonNode candidateMessage : candidateMessages) {
            String messageId = candidateMessage.path("id").asText("");
            if (messageId.isBlank()) {
                continue;
            }

            JsonNode message = getMessageMetadata(tokenSession.getAccessToken(), messageId);
            WarnMeEmailMetadata metadata = toWarnMeEmailMetadata(message);
            if (metadata.isExactWarnMeMatch()) {
                matches.add(metadata);
            }
        }

        return matches;
    }

    public List<RawWarnMeEmail> readMatchingWarnMeEmails(HttpSession session) {
        GmailTokenSession tokenSession = googleOAuthService.getUsableTokenSession(session);
        List<JsonNode> candidateMessages = listCandidateMessages(tokenSession.getAccessToken());
        List<RawWarnMeEmail> matches = new ArrayList<>();

        for (JsonNode candidateMessage : candidateMessages) {
            String messageId = candidateMessage.path("id").asText("");
            if (messageId.isBlank()) {
                continue;
            }

            JsonNode message = getFullMessage(tokenSession.getAccessToken(), messageId);
            WarnMeEmailMetadata metadata = toWarnMeEmailMetadata(message);
            if (metadata.isExactWarnMeMatch()) {
                matches.add(toRawWarnMeEmail(message, metadata));
            }
        }

        return matches;
    }

    public ParsedWarnMeEmail parseWarnMeEmail(WarnMeEmailMetadata message) {
        String title = message.getSubject();
        if (title != null && title.startsWith(WARNME_SUBJECT_PREFIX)) {
            title = title.substring(WARNME_SUBJECT_PREFIX.length()).trim();
            if (title.startsWith(":") || title.startsWith("-")) {
                title = title.substring(1).trim();
            }
        }

        if (title == null || title.isBlank()) {
            title = WARNME_SUBJECT_PREFIX;
        }

        // TODO: Extract incident type from the WarnMe email body.
        // TODO: Extract incident location and full description.
        // TODO: Classify severity from WarnMe content.
        // TODO: Geocode location into coordinates.
        return new ParsedWarnMeEmail(
                title,
                message.getSubject(),
                message.getSnippet(),
                message.getReceivedDate(),
                "gmail_warnme");
    }

    private List<JsonNode> listCandidateMessages(String accessToken) {
        List<JsonNode> messages = new ArrayList<>();
        String pageToken = null;

        do {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(GMAIL_MESSAGES_URL)
                    .queryParam("q", WARNME_GMAIL_QUERY)
                    .queryParam("maxResults", Math.min(50, MAX_CANDIDATE_MESSAGES - messages.size()));

            if (pageToken != null) {
                uriBuilder.queryParam("pageToken", pageToken);
            }

            JsonNode response = exchangeGmailGet(accessToken, uriBuilder.build().encode().toUri());
            if (response.has("messages")) {
                response.path("messages").forEach(messages::add);
            }

            pageToken = response.path("nextPageToken").asText(null);
        } while (pageToken != null && messages.size() < MAX_CANDIDATE_MESSAGES);

        return messages;
    }

    private JsonNode getMessageMetadata(String accessToken, String messageId) {
        URI url = UriComponentsBuilder.fromUriString(GMAIL_MESSAGES_URL + "/" + messageId)
                .queryParam("format", "metadata")
                .queryParam("metadataHeaders", "Subject")
                .queryParam("metadataHeaders", "From")
                .queryParam("metadataHeaders", "Date")
                .build()
                .encode()
                .toUri();

        return exchangeGmailGet(accessToken, url);
    }

    private JsonNode getFullMessage(String accessToken, String messageId) {
        URI url = UriComponentsBuilder.fromUriString(GMAIL_MESSAGES_URL + "/" + messageId)
                .queryParam("format", "full")
                .build()
                .encode()
                .toUri();

        return exchangeGmailGet(accessToken, url);
    }

    private WarnMeEmailMetadata toWarnMeEmailMetadata(JsonNode message) {
        String subject = findHeaderValue(message, "Subject");
        String sender = findHeaderValue(message, "From");
        String receivedDate = findHeaderValue(message, "Date");
        boolean exactMatch = isExactWarnMeMatch(subject, sender);

        WarnMeEmailMetadata metadata = new WarnMeEmailMetadata(
                message.path("id").asText(""),
                message.path("threadId").asText(""),
                subject,
                sender,
                receivedDate,
                message.path("snippet").asText(""),
                message.path("internalDate").asText(""),
                exactMatch,
                null);

        return new WarnMeEmailMetadata(
                metadata.getMessageId(),
                metadata.getThreadId(),
                metadata.getSubject(),
                metadata.getSender(),
                metadata.getReceivedDate(),
                metadata.getSnippet(),
                metadata.getInternalTimestamp(),
                metadata.isExactWarnMeMatch(),
                parseWarnMeEmail(metadata));
    }

    private boolean isExactWarnMeMatch(String subject, String sender) {
        String normalizedSubject = subject == null ? "" : subject.toLowerCase(Locale.ROOT);
        return !normalizedSubject.isBlank()
                && sender != null
                && sender.toLowerCase(Locale.ROOT).contains(WARNME_SENDER_EMAIL)
                && (normalizedSubject.startsWith(WARNME_SUBJECT_PREFIX.toLowerCase(Locale.ROOT))
                || normalizedSubject.contains("critical alert")
                || normalizedSubject.contains("critical alerts")
                || normalizedSubject.contains("avoid the area")
                || normalizedSubject.contains("all clear"));
    }

    private RawWarnMeEmail toRawWarnMeEmail(JsonNode message, WarnMeEmailMetadata metadata) {
        return new RawWarnMeEmail(
                metadata.getMessageId(),
                metadata.getThreadId(),
                metadata.getSubject(),
                metadata.getSender(),
                metadata.getReceivedDate(),
                metadata.getSnippet(),
                extractBodyText(message.path("payload")));
    }

    private String extractBodyText(JsonNode payload) {
        List<String> textParts = new ArrayList<>();
        collectBodyParts(payload, textParts);
        return String.join("\n\n", textParts).trim();
    }

    private void collectBodyParts(JsonNode part, List<String> textParts) {
        if (part == null || part.isMissingNode()) {
            return;
        }

        String mimeType = part.path("mimeType").asText("");
        String data = part.path("body").path("data").asText("");
        if (!data.isBlank() && ("text/plain".equalsIgnoreCase(mimeType) || textParts.isEmpty())) {
            String decoded = decodeBase64Url(data);
            if ("text/html".equalsIgnoreCase(mimeType)) {
                decoded = decoded.replaceAll("(?is)<br\\s*/?>", "\n")
                        .replaceAll("(?is)</p>", "\n\n")
                        .replaceAll("(?is)<[^>]+>", " ");
            }
            if (!decoded.isBlank()) {
                textParts.add(decoded.replaceAll("[ \\t]+", " ").trim());
            }
        }

        JsonNode parts = part.path("parts");
        if (parts.isArray()) {
            parts.forEach(childPart -> collectBodyParts(childPart, textParts));
        }
    }

    private String decodeBase64Url(String encodedValue) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedValue);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private String findHeaderValue(JsonNode message, String headerName) {
        JsonNode headers = message.path("payload").path("headers");
        if (!headers.isArray()) {
            return "";
        }

        for (JsonNode header : headers) {
            if (headerName.equalsIgnoreCase(header.path("name").asText())) {
                return header.path("value").asText("");
            }
        }

        return "";
    }

    private JsonNode exchangeGmailGet(String accessToken, URI url) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            JsonNode response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class).getBody();
            if (response == null) {
                throw new ResponseStatusException(BAD_GATEWAY, "Gmail API returned an empty response.");
            }
            return response;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Gmail API request failed.", exception);
        }
    }
}
