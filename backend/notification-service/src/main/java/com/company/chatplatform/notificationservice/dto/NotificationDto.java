package com.company.chatplatform.notificationservice.dto;

public class NotificationDto {
    private String id;
    private String recipientId;
    private String title;
    private String body;
    private String type;
    private boolean read;
    private String createdAt;

    public NotificationDto() {}

    public NotificationDto(String id, String recipientId, String title, String body, String type, boolean read, String createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.title = title;
        this.body = body;
        this.type = type;
        this.read = read;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getType() {
        return type;
    }

    public boolean isRead() {
        return read;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
