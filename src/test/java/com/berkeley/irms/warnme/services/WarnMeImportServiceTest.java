package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.dto.gmail.RawWarnMeEmail;
import com.berkeley.irms.warnme.dto.warnme.GeocodingResult;
import com.berkeley.irms.warnme.dto.warnme.ParsedWarnMeIncident;
import com.berkeley.irms.warnme.dto.warnme.WarnMeImportSummary;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WarnMeImportServiceTest {

    @Test
    void skipsDuplicateGmailMessageId() {
        WarnMeGmailService gmailService = mock(WarnMeGmailService.class);
        WarnMeEmailParser parser = mock(WarnMeEmailParser.class);
        OpenAIWarnMeParserService aiParser = mock(OpenAIWarnMeParserService.class);
        GeocodingService geocodingService = mock(GeocodingService.class);
        IncidentService incidentService = mock(IncidentService.class);
        HttpSession session = mock(HttpSession.class);
        RawWarnMeEmail email = new RawWarnMeEmail(
                "gmail-1",
                "thread-1",
                "UC Berkeley WarnMe",
                "ucberkeley@warnme.berkeley.edu",
                "",
                "",
                "body");

        when(gmailService.readMatchingWarnMeEmails(session)).thenReturn(List.of(email));
        when(incidentService.existsByGmailMessageId("gmail-1")).thenReturn(true);

        WarnMeImportService importService = new WarnMeImportService(gmailService, parser, aiParser, geocodingService, incidentService);

        WarnMeImportSummary summary = importService.importWarnMeEmails(session);

        assertThat(summary.getEmailsFound()).isEqualTo(1);
        assertThat(summary.getDuplicateCount()).isEqualTo(1);
        assertThat(summary.getSavedCount()).isZero();
        verify(incidentService, never()).createIncident(any());
    }

    @Test
    void savesParsedGeocodedWarnMeIncident() {
        WarnMeGmailService gmailService = mock(WarnMeGmailService.class);
        WarnMeEmailParser parser = mock(WarnMeEmailParser.class);
        OpenAIWarnMeParserService aiParser = mock(OpenAIWarnMeParserService.class);
        GeocodingService geocodingService = mock(GeocodingService.class);
        IncidentService incidentService = mock(IncidentService.class);
        HttpSession session = mock(HttpSession.class);
        RawWarnMeEmail email = new RawWarnMeEmail(
                "gmail-1",
                "thread-1",
                "UC Berkeley WarnMe",
                "ucberkeley@warnme.berkeley.edu",
                "",
                "",
                "body");
        ParsedWarnMeIncident parsed = new ParsedWarnMeIncident(
                "aggravated_assault",
                "Aggravated Assault",
                "Aggravated Assault",
                "description",
                "2026-04-05",
                "22:19",
                "22:24",
                "South Drive near Gilman Hall, UC Berkeley, Berkeley, CA",
                "South Drive near Gilman Hall",
                "high",
                "active",
                "body");

        when(gmailService.readMatchingWarnMeEmails(session)).thenReturn(List.of(email));
        when(incidentService.existsByGmailMessageId("gmail-1")).thenReturn(false);
        when(parser.parseWarnMeEmail(email)).thenReturn(parsed);
        when(aiParser.parseWarnMeEmailWithAI(email, parsed)).thenReturn(Optional.empty());
        when(geocodingService.geocodeLocation(parsed.getApproximateLocation()))
                .thenReturn(GeocodingResult.success(37.8726, -122.2565, parsed.getApproximateLocation(), "test"));

        WarnMeImportService importService = new WarnMeImportService(gmailService, parser, aiParser, geocodingService, incidentService);

        WarnMeImportSummary summary = importService.importWarnMeEmails(session);

        assertThat(summary.getSavedCount()).isEqualTo(1);
        assertThat(summary.getGeocodedCount()).isEqualTo(1);
        verify(incidentService).createIncident(any());
    }
}
