package com.berkeley.irms.warnme.dto.warnme;

public class ParsedWarnMeIncident {

    private final String incidentType;
    private final String displayType;
    private final String title;
    private final String description;
    private final String incidentDate;
    private final String incidentTime;
    private final String reportedTime;
    private final String approximateLocation;
    private final String locationText;
    private final String severity;
    private final String alertStatus;
    private final String rawText;

    public ParsedWarnMeIncident(
            String incidentType,
            String displayType,
            String title,
            String description,
            String incidentDate,
            String incidentTime,
            String reportedTime,
            String approximateLocation,
            String locationText,
            String severity,
            String alertStatus,
            String rawText) {
        this.incidentType = incidentType;
        this.displayType = displayType;
        this.title = title;
        this.description = description;
        this.incidentDate = incidentDate;
        this.incidentTime = incidentTime;
        this.reportedTime = reportedTime;
        this.approximateLocation = approximateLocation;
        this.locationText = locationText;
        this.severity = severity;
        this.alertStatus = alertStatus;
        this.rawText = rawText;
    }

    public String getIncidentType() {
        return incidentType;
    }

    public String getDisplayType() {
        return displayType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIncidentDate() {
        return incidentDate;
    }

    public String getIncidentTime() {
        return incidentTime;
    }

    public String getReportedTime() {
        return reportedTime;
    }

    public String getApproximateLocation() {
        return approximateLocation;
    }

    public String getLocationText() {
        return locationText;
    }

    public String getSeverity() {
        return severity;
    }

    public String getAlertStatus() {
        return alertStatus;
    }

    public String getRawText() {
        return rawText;
    }
}
