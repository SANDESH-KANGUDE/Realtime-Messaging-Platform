package com.company.chatplatform.messageservice.domain.document;

import java.time.Instant;

public class ReactionDocument {
    private String userId;
    private String emoji;
    private Instant createdAt = Instant.now();

    public ReactionDocument() {}

    public ReactionDocument(String userId, String emoji) {
        this.userId = userId;
        this.emoji = emoji;
        this.createdAt = Instant.now();
    }

    public String getUserId() {
        return userId;
    }

    public String getEmoji() {
        return emoji;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
