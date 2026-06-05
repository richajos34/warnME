package com.berkeley.irms.warnme.services;

import com.berkeley.irms.warnme.dto.gmail.RawWarnMeEmail;
import com.berkeley.irms.warnme.dto.warnme.ParsedWarnMeIncident;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WarnMeEmailParserTest {

    private static final String SAMPLE_BODY = """
            On April 5, 2026 at 2219 hours, an aggravated assault occurred on South Drive near Gilman Hall. The incident was reported to UCPD at approximately 2224 hours.

            Several subjects in an older silver Nissan sedan, drove east on South Drive near Gilman Hall. The rear passenger pointed a reported silver handgun at a jogger who was in the area. The jogger saw the handgun then fled the area and called UCPD. The subjects fled the area.

            Aggravated assault is an unlawful attack by one person upon another for the purpose of inflicting severe or aggravated bodily injury. This type of assault is usually accompanied by the use of a weapon or by means likely to produce death or great bodily harm.
            """;

    private final WarnMeEmailParser parser = new WarnMeEmailParser();

    @Test
    void parsesSampleWarnMeEmail() {
        RawWarnMeEmail email = new RawWarnMeEmail(
                "gmail-1",
                "thread-1",
                "UC Berkeley WarnMe: Aggravated Assault",
                "UC Berkeley WarnMe Emergency Notification <ucberkeley@warnme.berkeley.edu>",
                "Sun, 5 Apr 2026 22:30:00 -0700",
                "",
                SAMPLE_BODY);

        ParsedWarnMeIncident parsed = parser.parseWarnMeEmail(email);

        assertThat(parsed.getIncidentDate()).isEqualTo("2026-04-05");
        assertThat(parsed.getIncidentTime()).isEqualTo("22:19");
        assertThat(parsed.getReportedTime()).isEqualTo("22:24");
        assertThat(parsed.getIncidentType()).isEqualTo("aggravated_assault");
        assertThat(parsed.getDisplayType()).isEqualTo("Aggravated Assault");
        assertThat(parsed.getSeverity()).isEqualTo("high");
        assertThat(parsed.getAlertStatus()).isEqualTo("active");
        assertThat(parsed.getApproximateLocation()).contains("South Drive near Gilman Hall");
        assertThat(parsed.getApproximateLocation()).contains("UC Berkeley, Berkeley, CA");
        assertThat(parsed.getDescription()).contains("Several subjects in an older silver Nissan sedan");
    }

    @Test
    void marksAllClearAsClosed() {
        RawWarnMeEmail email = new RawWarnMeEmail(
                "gmail-2",
                "thread-2",
                "UC Berkeley WarnMe: ALL CLEAR",
                "UC Berkeley WarnMe Emergency Notification <ucberkeley@warnme.berkeley.edu>",
                "Sun, 5 Apr 2026 23:30:00 -0700",
                "",
                "ALL CLEAR. The incident near South Drive and Gilman Hall has concluded.");

        ParsedWarnMeIncident parsed = parser.parseWarnMeEmail(email);

        assertThat(parsed.getAlertStatus()).isEqualTo("closed");
    }
}
