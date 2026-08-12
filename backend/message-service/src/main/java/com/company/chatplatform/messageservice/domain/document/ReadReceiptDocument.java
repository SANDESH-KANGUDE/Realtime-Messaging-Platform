package com.company.chatplatform.messageservice.domain.document;

import java.time.Instant;

public class ReadReceiptDocument {
    private String userId;
    private Instant readAt = Instant.now();

    public ReadReceiptDocument() {}

    public ReadReceiptDocument(String userId) {
        this.userId = userId;
        this.readAt = Instant.now();
    }

    public String getUserId() {
        return userId;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
