package com.berkeley.irms.warnme.dto.warnme;

import java.util.ArrayList;
import java.util.List;

public class WarnMeImportSummary {

    private int emailsFound;
    private int parsedCount;
    private int geocodedCount;
    private int savedCount;
    private int duplicateCount;
    private int failedCount;
    private final List<WarnMeImportFailure> failures = new ArrayList<>();

    public int getEmailsFound() {
        return emailsFound;
    }

    public void setEmailsFound(int emailsFound) {
        this.emailsFound = emailsFound;
    }

    public int getParsedCount() {
        return parsedCount;
    }

    public void incrementParsedCount() {
        this.parsedCount++;
    }

    public int getGeocodedCount() {
        return geocodedCount;
    }

    public void incrementGeocodedCount() {
        this.geocodedCount++;
    }

    public int getSavedCount() {
        return savedCount;
    }

    public void incrementSavedCount() {
        this.savedCount++;
    }

    public int getDuplicateCount() {
        return duplicateCount;
    }

    public void incrementDuplicateCount() {
        this.duplicateCount++;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public List<WarnMeImportFailure> getFailures() {
        return failures;
    }

    public void addFailure(String gmailMessageId, String reason) {
        this.failedCount++;
        this.failures.add(new WarnMeImportFailure(gmailMessageId, reason));
    }
}
