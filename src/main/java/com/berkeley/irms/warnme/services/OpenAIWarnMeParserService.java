package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.dto.gmail.RawWarnMeEmail;
import com.berkeley.irms.warnme.dto.warnme.ParsedWarnMeIncident;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OpenAIWarnMeParserService {

    private static final String RESPONSES_URL = "https://api.openai.com/v1/responses";

    private final String apiKey;
    private final String model;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public OpenAIWarnMeParserService(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4.1-mini}") String model) {
        this(apiKey, model, new RestTemplate());
    }

    OpenAIWarnMeParserService(String apiKey, String model, RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.model = model;
        this.restTemplate = restTemplate;
    }

    public Optional<ParsedWarnMeIncident> parseWarnMeEmailWithAI(RawWarnMeEmail email, ParsedWarnMeIncident fallback) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> request = Map.of(
                "model", model,
                "instructions", buildInstructions(),
                "input", buildInput(email, fallback),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "warnme_incident",
                                "strict", true,
                                "schema", buildSchema())));

        try {
            JsonNode response = restTemplate.postForObject(RESPONSES_URL, new HttpEntity<>(request, headers), JsonNode.class);
            String jsonText = extractOutputText(response);
            if (jsonText == null || jsonText.isBlank()) {
                return Optional.empty();
            }

            JsonNode parsed = objectMapper.readTree(jsonText);
            return toValidatedIncident(parsed, fallback);
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private String buildInstructions() {
        return """
                Extract a UC Berkeley WarnMe alert into strict JSON. Only use the supplied email.
                Preserve privacy: do not invent unrelated information.
                If the email says ALL CLEAR, closure, resolved, no longer a threat, or incident concluded, set alertStatus to closed.
                If the subject/body says Critical Alert or avoid the area, set alertStatus active and severity high or critical.
                """;
    }

    private String buildInput(RawWarnMeEmail email, ParsedWarnMeIncident fallback) {
        return """
                Subject: %s
                From: %s
                Received: %s
                Snippet: %s

                Body:
                %s

                Deterministic fallback:
                incidentType=%s
                displayType=%s
                incidentDate=%s
                incidentTime=%s
                location=%s
                severity=%s
                alertStatus=%s
                """.formatted(
                email.getSubject(),
                email.getSender(),
                email.getReceivedAt(),
                email.getSnippet(),
                email.getBody(),
                fallback.getIncidentType(),
                fallback.getDisplayType(),
                fallback.getIncidentDate(),
                fallback.getIncidentTime(),
                fallback.getApproximateLocation(),
                fallback.getSeverity(),
                fallback.getAlertStatus());
    }

    private Map<String, Object> buildSchema() {
        Map<String, Object> stringType = Map.of("type", "string");
        Map<String, Object> nullableStringType = Map.of("type", List.of("string", "null"));
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of(
                        "incidentType",
                        "displayType",
                        "title",
                        "description",
                        "incidentDate",
                        "incidentTime",
                        "reportedTime",
                        "approximateLocation",
                        "locationText",
                        "severity",
                        "alertStatus"),
                "properties", Map.ofEntries(
                        Map.entry("incidentType", stringType),
                        Map.entry("displayType", stringType),
                        Map.entry("title", stringType),
                        Map.entry("description", stringType),
                        Map.entry("incidentDate", nullableStringType),
                        Map.entry("incidentTime", nullableStringType),
                        Map.entry("reportedTime", nullableStringType),
                        Map.entry("approximateLocation", stringType),
                        Map.entry("locationText", stringType),
                        Map.entry("severity", Map.of("type", "string", "enum", List.of("low", "medium", "high", "critical"))),
                        Map.entry("alertStatus", Map.of("type", "string", "enum", List.of("active", "mild", "closed")))));
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            return null;
        }

        JsonNode output = response.path("output");
        if (!output.isArray()) {
            return null;
        }

        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }

            for (JsonNode contentItem : content) {
                String text = contentItem.path("text").asText("");
                if (!text.isBlank()) {
                    return text;
                }
            }
        }

        return response.path("output_text").asText(null);
    }

    private Optional<ParsedWarnMeIncident> toValidatedIncident(JsonNode parsed, ParsedWarnMeIncident fallback) {
        String incidentType = textOrFallback(parsed, "incidentType", fallback.getIncidentType());
        String displayType = textOrFallback(parsed, "displayType", fallback.getDisplayType());
        String title = textOrFallback(parsed, "title", fallback.getTitle());
        String description = textOrFallback(parsed, "description", fallback.getDescription());
        String approximateLocation = textOrFallback(parsed, "approximateLocation", fallback.getApproximateLocation());
        String locationText = textOrFallback(parsed, "locationText", fallback.getLocationText());
        String severity = enumOrFallback(parsed, "severity", List.of("low", "medium", "high", "critical"), fallback.getSeverity());
        String alertStatus = enumOrFallback(parsed, "alertStatus", List.of("active", "mild", "closed"), fallback.getAlertStatus());

        if (incidentType.isBlank() || displayType.isBlank() || title.isBlank() || approximateLocation.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(new ParsedWarnMeIncident(
                incidentType,
                displayType,
                title,
                description,
                nullableText(parsed, "incidentDate", fallback.getIncidentDate()),
                nullableText(parsed, "incidentTime", fallback.getIncidentTime()),
                nullableText(parsed, "reportedTime", fallback.getReportedTime()),
                approximateLocation,
                locationText,
                severity,
                alertStatus,
                fallback.getRawText()));
    }

    private String textOrFallback(JsonNode node, String fieldName, String fallback) {
        String value = node.path(fieldName).asText("");
        return value.isBlank() ? fallback : value;
    }

    private String nullableText(JsonNode node, String fieldName, String fallback) {
        if (node.path(fieldName).isNull()) {
            return fallback;
        }
        return textOrFallback(node, fieldName, fallback);
    }

    private String enumOrFallback(JsonNode node, String fieldName, List<String> allowedValues, String fallback) {
        String value = node.path(fieldName).asText("");
        return allowedValues.contains(value) ? value : fallback;
    }
}
