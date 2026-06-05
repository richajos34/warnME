package com.berkeley.irms.warnme.dto.gmail;

public class RawWarnMeEmail {

    private final String messageId;
    private final String threadId;
    private final String subject;
    private final String sender;
    private final String receivedAt;
    private final String snippet;
    private final String body;

    public RawWarnMeEmail(
            String messageId,
            String threadId,
            String subject,
            String sender,
            String receivedAt,
            String snippet,
            String body) {
        this.messageId = messageId;
        this.threadId = threadId;
        this.subject = subject;
        this.sender = sender;
        this.receivedAt = receivedAt;
        this.snippet = snippet;
        this.body = body;
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

    public String getReceivedAt() {
        return receivedAt;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getBody() {
        return body;
    }
}
