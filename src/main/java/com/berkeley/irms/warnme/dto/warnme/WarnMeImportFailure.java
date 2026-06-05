package com.berkeley.irms.warnme.dto.warnme;

public class WarnMeImportFailure {

    private final String gmailMessageId;
    private final String reason;

    public WarnMeImportFailure(String gmailMessageId, String reason) {
        this.gmailMessageId = gmailMessageId;
        this.reason = reason;
    }

    public String getGmailMessageId() {
        return gmailMessageId;
    }

    public String getReason() {
        return reason;
    }
}
