package com.berkeley.irms.warnme.dto.gmail;

public class WarnMeEmailMetadata {

    private final String messageId;
    private final String threadId;
    private final String subject;
    private final String sender;
    private final String receivedDate;
    private final String snippet;
    private final String internalTimestamp;
    private final boolean exactWarnMeMatch;
    private final ParsedWarnMeEmail parsed;

    public WarnMeEmailMetadata(
            String messageId,
            String threadId,
            String subject,
            String sender,
            String receivedDate,
            String snippet,
            String internalTimestamp,
            boolean exactWarnMeMatch,
            ParsedWarnMeEmail parsed) {
        this.messageId = messageId;
        this.threadId = threadId;
        this.subject = subject;
        this.sender = sender;
        this.receivedDate = receivedDate;
        this.snippet = snippet;
        this.internalTimestamp = internalTimestamp;
        this.exactWarnMeMatch = exactWarnMeMatch;
        this.parsed = parsed;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getSubject() {
        return subject;
    }

    public String getSender() {
        return sender;
    }

    public String getReceivedDate() {
        return receivedDate;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getInternalTimestamp() {
        return internalTimestamp;
    }

    public boolean isExactWarnMeMatch() {
        return exactWarnMeMatch;
    }

    public ParsedWarnMeEmail getParsed() {
        return parsed;
    }
}
