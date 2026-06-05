package com.berkeley.irms.warnme.controllers;

import com.berkeley.irms.warnme.dto.gmail.RawWarnMeEmail;
import com.berkeley.irms.warnme.dto.warnme.ParsedWarnMeIncident;
import com.berkeley.irms.warnme.services.WarnMeEmailParser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/warnme")
public class WarnMeParserController {

    private final WarnMeEmailParser warnMeEmailParser;

    public WarnMeParserController(WarnMeEmailParser warnMeEmailParser) {
        this.warnMeEmailParser = warnMeEmailParser;
    }

    @PostMapping("/parse-preview")
    public ParsedWarnMeIncident parsePreview(@RequestBody Map<String, String> payload) {
        RawWarnMeEmail email = new RawWarnMeEmail(
                payload.getOrDefault("gmailMessageId", "preview"),
                payload.getOrDefault("gmailThreadId", "preview"),
                payload.getOrDefault("subject", "UC Berkeley WarnMe"),
                payload.getOrDefault("from", "UC Berkeley WarnMe Emergency Notification <ucberkeley@warnme.berkeley.edu>"),
                payload.getOrDefault("receivedAt", ""),
                payload.getOrDefault("snippet", ""),
                payload.getOrDefault("body", payload.getOrDefault("rawText", "")));

        return warnMeEmailParser.parseWarnMeEmail(email);
    }
}
