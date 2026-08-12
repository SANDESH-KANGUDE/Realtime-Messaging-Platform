package com.company.chatplatform.messageservice.dto;

public class ReactionDto {
    private String userId;
    private String emoji;
    private String createdAt;

    public ReactionDto() {}

    public ReactionDto(String userId, String emoji, String createdAt) {
        this.userId = userId;
        this.emoji = emoji;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmoji() {
        return emoji;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
