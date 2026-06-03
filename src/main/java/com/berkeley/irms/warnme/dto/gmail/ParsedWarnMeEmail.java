package com.berkeley.irms.warnme.dto.gmail;

public class ParsedWarnMeEmail {

    private final String title;
    private final String rawSubject;
    private final String snippet;
    private final String receivedDate;
    private final String source;

    public ParsedWarnMeEmail(String title, String rawSubject, String snippet, String receivedDate, String source) {
        this.title = title;
        this.rawSubject = rawSubject;
        this.snippet = snippet;
        this.receivedDate = receivedDate;
        this.source = source;
    }

    public String getTitle() {
        return title;
    }

    public String getRawSubject() {
        return rawSubject;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public String getSource() {
        return source;
    }
}
