package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.dto.gmail.RawWarnMeEmail;
import com.berkeley.irms.warnme.dto.warnme.GeocodingResult;
import com.berkeley.irms.warnme.dto.warnme.ParsedWarnMeIncident;
import com.berkeley.irms.warnme.dto.warnme.WarnMeImportSummary;
import com.berkeley.irms.warnme.models.Incident;
import com.berkeley.irms.warnme.models.Location;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WarnMeImportService {

    private final WarnMeGmailService warnMeGmailService;
    private final WarnMeEmailParser warnMeEmailParser;
    private final OpenAIWarnMeParserService openAIWarnMeParserService;
    private final GeocodingService geocodingService;
    private final IncidentService incidentService;

    public WarnMeImportService(
            WarnMeGmailService warnMeGmailService,
            WarnMeEmailParser warnMeEmailParser,
            OpenAIWarnMeParserService openAIWarnMeParserService,
            GeocodingService geocodingService,
            IncidentService incidentService) {
        this.warnMeGmailService = warnMeGmailService;
        this.warnMeEmailParser = warnMeEmailParser;
        this.openAIWarnMeParserService = openAIWarnMeParserService;
        this.geocodingService = geocodingService;
        this.incidentService = incidentService;
    }

    public WarnMeImportSummary importWarnMeEmails(HttpSession session) {
        List<RawWarnMeEmail> emails = warnMeGmailService.readMatchingWarnMeEmails(session);
        WarnMeImportSummary summary = new WarnMeImportSummary();
        summary.setEmailsFound(emails.size());

        for (RawWarnMeEmail email : emails) {
            try {
                if (incidentService.existsByGmailMessageId(email.getMessageId())) {
                    summary.incrementDuplicateCount();
                    continue;
                }

                ParsedWarnMeIncident deterministicIncident = warnMeEmailParser.parseWarnMeEmail(email);
                ParsedWarnMeIncident parsedIncident = openAIWarnMeParserService
                        .parseWarnMeEmailWithAI(email, deterministicIncident)
                        .orElse(deterministicIncident);
                summary.incrementParsedCount();

                GeocodingResult geocodingResult = geocodingService.geocodeLocation(parsedIncident.getApproximateLocation());
                if (geocodingResult.hasCoordinates()) {
                    summary.incrementGeocodedCount();
                }

                if ("closed".equals(parsedIncident.getAlertStatus())) {
                    Incident closedIncident = incidentService
                            .findOpenWarnMeIncident(parsedIncident.getIncidentType(), parsedIncident.getLocationText())
                            .map(existingIncident -> updateClosedIncident(existingIncident, email, parsedIncident))
                            .orElseGet(() -> toIncident(email, parsedIncident, geocodingResult));
                    incidentService.createIncident(closedIncident);
                } else {
                    incidentService.createIncident(toIncident(email, parsedIncident, geocodingResult));
                }
                summary.incrementSavedCount();
            } catch (DuplicateKeyException exception) {
                summary.incrementDuplicateCount();
            } catch (RuntimeException exception) {
                summary.addFailure(email.getMessageId(), exception.getMessage());
            }
        }

        return summary;
    }

    public Incident toIncident(
            RawWarnMeEmail email,
            ParsedWarnMeIncident parsedIncident,
            GeocodingResult geocodingResult) {
        Instant now = Instant.now();
        Incident incident = new Incident();

        incident.setTitle(parsedIncident.getTitle());
        incident.setIncidentType(parsedIncident.getIncidentType());
        incident.setDisplayType(parsedIncident.getDisplayType());
        incident.setDescription(parsedIncident.getDescription());
        incident.setIncidentDate(parsedIncident.getIncidentDate());
        incident.setIncidentTime(parsedIncident.getIncidentTime());
        incident.setReportedTime(parsedIncident.getReportedTime());
        incident.setApproximateLocation(parsedIncident.getApproximateLocation());
        incident.setLocationText(parsedIncident.getLocationText());
        incident.setSeverity(parsedIncident.getSeverity());
        incident.setAlertStatus(parsedIncident.getAlertStatus());
        incident.setSource("gmail_warnme");
        incident.setVerificationStatus("official_warnme");
        incident.setRawText(parsedIncident.getRawText());
        incident.setImportedAt(now);
        incident.setCreatedAt(now);
        incident.setUpdatedAt(now);
        incident.setStatus(toStatusLabel(parsedIncident.getAlertStatus()));
        incident.setType(parsedIncident.getDisplayType());
        incident.setTimestamp(buildTimestamp(parsedIncident));
        incident.setSourceMetadata(new Incident.SourceMetadata(
                email.getMessageId(),
                email.getThreadId(),
                email.getSubject(),
                email.getSender(),
                email.getReceivedAt()));

        incident.setGeocodingStatus(geocodingResult.getStatus());
        incident.setGeocodingSource(geocodingResult.getSource());
        incident.setFormattedAddress(geocodingResult.getFormattedAddress());

        if (geocodingResult.hasCoordinates()) {
            incident.setLocation(new Location(geocodingResult.getLat().floatValue(), geocodingResult.getLng().floatValue()));
            incident.setCoordinates(new Incident.GeoJsonPoint(geocodingResult.getLng(), geocodingResult.getLat()));
        } else {
            incident.setGeocodingStatus("failed");
        }

        return incident;
    }

    private Incident updateClosedIncident(
            Incident incident,
            RawWarnMeEmail email,
            ParsedWarnMeIncident parsedIncident) {
        Instant now = Instant.now();
        incident.setAlertStatus("closed");
        incident.setSeverity("low");
        incident.setStatus(toStatusLabel("closed"));
        incident.setTitle(parsedIncident.getTitle());
        incident.setDisplayType(parsedIncident.getDisplayType());
        incident.setDescription(parsedIncident.getDescription());
        incident.setVerificationStatus("official_warnme");
        incident.setRawText(parsedIncident.getRawText());
        incident.setUpdatedAt(now);
        incident.setSourceMetadata(new Incident.SourceMetadata(
                email.getMessageId(),
                email.getThreadId(),
                email.getSubject(),
                email.getSender(),
                email.getReceivedAt()));
        return incident;
    }

    private String buildTimestamp(ParsedWarnMeIncident parsedIncident) {
        if (parsedIncident.getIncidentDate() == null || parsedIncident.getIncidentTime() == null) {
            return parsedIncident.getIncidentDate() == null ? "" : parsedIncident.getIncidentDate();
        }

        return parsedIncident.getIncidentDate() + " " + parsedIncident.getIncidentTime() + ":00";
    }

    private String toStatusLabel(String alertStatus) {
        if ("closed".equals(alertStatus)) {
            return "Closed";
        }

        if ("mild".equals(alertStatus)) {
            return "Mild WarnMe";
        }

        return "Active WarnMe";
    }
}
