package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.dto.gmail.RawWarnMeEmail;
import com.berkeley.irms.warnme.dto.warnme.ParsedWarnMeIncident;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WarnMeEmailParser {

    private static final Pattern DATE_AND_TIME_PATTERN = Pattern.compile(
            "(?i)\\bOn\\s+([A-Z][a-z]+\\s+\\d{1,2},\\s+\\d{4})\\s+at\\s+(\\d{3,4})\\s+hours?");
    private static final Pattern REPORTED_TIME_PATTERN = Pattern.compile(
            "(?i)reported\\s+to\\s+UCPD\\s+at\\s+(?:approximately\\s+)?(\\d{3,4})\\s+hours?");
    private static final Pattern OCCURRED_LOCATION_PATTERN = Pattern.compile(
            "(?i)\\b(?:occurred|reported)\\s+(?:on|at|near)\\s+(.+?)(?:\\.|\\n)");
    private static final DateTimeFormatter BODY_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMMM d, yyyy")
            .toFormatter(Locale.US);

    private static final Map<String, TypeDefinition> TYPE_DEFINITIONS = new LinkedHashMap<>();
    private static final List<String> DESCRIPTION_STOP_PHRASES = List.of(
            "If you have any information",
            "UCPD would like to remind",
            "In case of injury",
            "Safety tips",
            "Safety Tips",
            "Medical options");

    static {
        registerType("aggravated assault", "aggravated_assault", "Aggravated Assault");
        registerType("vehicle break-in", "vehicle_break_in", "Vehicle Break-In");
        registerType("vehicle theft", "vehicle_theft", "Vehicle Theft");
        registerType("suspicious person", "suspicious_person", "Suspicious Person");
        registerType("sexual assault", "sexual_assault", "Sexual Assault");
        registerType("armed robbery", "robbery", "Robbery");
        registerType("robbery", "robbery", "Robbery");
        registerType("burglary", "burglary", "Burglary");
        registerType("theft", "theft", "Theft");
        registerType("shooting", "shooting", "Shooting");
        registerType("stabbing", "stabbing", "Stabbing");
        registerType("arson", "arson", "Arson");
        registerType("emergency", "emergency", "Emergency");
    }

    public ParsedWarnMeIncident parseWarnMeEmail(RawWarnMeEmail email) {
        String body = normalizeBody(email.getBody());
        String subject = email.getSubject() == null ? "" : email.getSubject();
        String combinedText = body + " " + subject;
        String incidentDate = extractIncidentDate(body).orElseGet(() -> fallbackDate(email.getReceivedAt()));
        String incidentTime = extractIncidentTime(body).orElse(null);
        String reportedTime = extractReportedTime(body).orElse(null);
        TypeDefinition type = extractIncidentType(combinedText);
        String locationText = extractLocationText(body).orElse("UC Berkeley campus");
        String approximateLocation = appendBerkeleyContext(locationText);
        String description = cleanupDescription(body, email.getSnippet());
        String severity = inferSeverity(combinedText, type.normalizedType());
        String alertStatus = inferAlertStatus(combinedText, severity);
        String title = inferTitle(subject, type.displayType(), alertStatus);

        return new ParsedWarnMeIncident(
                type.normalizedType(),
                type.displayType(),
                title,
                description,
                incidentDate,
                incidentTime,
                reportedTime,
                approximateLocation,
                locationText,
                severity,
                alertStatus,
                body);
    }

    private Optional<String> extractIncidentDate(String body) {
        Matcher matcher = DATE_AND_TIME_PATTERN.matcher(body);
        if (!matcher.find()) {
            return Optional.empty();
        }

        try {
            LocalDate parsedDate = LocalDate.parse(matcher.group(1), BODY_DATE_FORMATTER);
            return Optional.of(parsedDate.toString());
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    private Optional<String> extractIncidentTime(String body) {
        Matcher matcher = DATE_AND_TIME_PATTERN.matcher(body);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(formatMilitaryTime(matcher.group(2)));
    }

    private Optional<String> extractReportedTime(String body) {
        Matcher matcher = REPORTED_TIME_PATTERN.matcher(body);
        if (!matcher.find()) {
            return Optional.empty();
        }

        return Optional.of(formatMilitaryTime(matcher.group(1)));
    }

    private TypeDefinition extractIncidentType(String text) {
        String normalizedText = text.toLowerCase(Locale.ROOT);
        return TYPE_DEFINITIONS.entrySet().stream()
                .filter(entry -> normalizedText.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .max(Comparator.comparingInt(definition -> definition.phrase().length()))
                .orElse(new TypeDefinition("other", "Other", "other"));
    }

    private Optional<String> extractLocationText(String body) {
        Matcher matcher = OCCURRED_LOCATION_PATTERN.matcher(body);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String location = matcher.group(1)
                .replaceAll("(?i)^the\\s+", "")
                .replaceAll("\\s+", " ")
                .trim();
        return location.isBlank() ? Optional.empty() : Optional.of(location);
    }

    private String cleanupDescription(String body, String snippet) {
        if (body == null || body.isBlank()) {
            return snippet == null ? "" : snippet;
        }

        String cleaned = body;
        for (String stopPhrase : DESCRIPTION_STOP_PHRASES) {
            int stopIndex = cleaned.toLowerCase(Locale.ROOT).indexOf(stopPhrase.toLowerCase(Locale.ROOT));
            if (stopIndex >= 0) {
                cleaned = cleaned.substring(0, stopIndex);
            }
        }

        String[] paragraphs = cleaned.split("\\n\\s*\\n");
        StringBuilder description = new StringBuilder();
        int meaningfulParagraphs = 0;

        for (String paragraph : paragraphs) {
            String normalizedParagraph = paragraph.replaceAll("\\s+", " ").trim();
            if (normalizedParagraph.isBlank()) {
                continue;
            }

            if (meaningfulParagraphs > 0) {
                description.append("\n\n");
            }
            description.append(normalizedParagraph);
            meaningfulParagraphs++;

            if (meaningfulParagraphs >= 2) {
                break;
            }
        }

        return description.isEmpty() ? cleaned.replaceAll("\\s+", " ").trim() : description.toString();
    }

    private String inferSeverity(String text, String incidentType) {
        String normalizedText = text.toLowerCase(Locale.ROOT);

        if (containsAny(normalizedText, "shooting", "stabbing", "homicide", "active shooter", "armed robbery", "firearm discharged")) {
            return "critical";
        }

        if (containsAny(normalizedText, "critical alert", "critical alerts", "avoid the area")
                || "aggravated_assault".equals(incidentType)
                || containsAny(normalizedText, "handgun", " gun", "weapon", "robbery", "sexual assault")) {
            return "high";
        }

        if (containsAny(normalizedText, "burglary", "vehicle theft", "vehicle break-in", "suspicious person")) {
            return "medium";
        }

        if (containsAny(normalizedText, "theft", "suspicious activity", "property crime")) {
            return "low";
        }

        return "medium";
    }

    private String inferAlertStatus(String text, String severity) {
        String normalizedText = text.toLowerCase(Locale.ROOT);

        if (containsAny(normalizedText, "all clear", "shelter in place lifted", "avoid the area lifted",
                "incident has concluded", "no longer a threat", "resolved", "closure")) {
            return "closed";
        }

        if ("critical".equals(severity) || "high".equals(severity)
                || containsAny(normalizedText, "critical alert", "critical alerts", "avoid the area")) {
            return "active";
        }

        return "mild";
    }

    private String inferTitle(String subject, String displayType, String alertStatus) {
        String cleanedSubject = subject == null ? "" : subject
                .replaceFirst("(?i)^UC Berkeley WarnMe\\s*[:\\-]?\\s*", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (!cleanedSubject.isBlank()) {
            return cleanedSubject;
        }

        if ("closed".equals(alertStatus)) {
            return "All Clear";
        }

        return displayType;
    }

    private String fallbackDate(String receivedAt) {
        if (receivedAt == null || receivedAt.isBlank()) {
            return LocalDate.now().toString();
        }

        try {
            return ZonedDateTime.parse(receivedAt, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDate().toString();
        } catch (DateTimeParseException exception) {
            return LocalDate.now().toString();
        }
    }

    private String appendBerkeleyContext(String locationText) {
        String normalizedLocation = locationText.trim();
        if (normalizedLocation.toLowerCase(Locale.ROOT).contains("berkeley")) {
            return normalizedLocation;
        }

        return normalizedLocation + ", UC Berkeley, Berkeley, CA";
    }

    private String formatMilitaryTime(String value) {
        String padded = value.length() == 3 ? "0" + value : value;
        return padded.substring(0, 2) + ":" + padded.substring(2);
    }

    private String normalizeBody(String body) {
        return body == null ? "" : body.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private static void registerType(String phrase, String normalizedType, String displayType) {
        TYPE_DEFINITIONS.put(phrase, new TypeDefinition(phrase, normalizedType, displayType));
    }

    private record TypeDefinition(String phrase, String normalizedType, String displayType) {
    }
}
