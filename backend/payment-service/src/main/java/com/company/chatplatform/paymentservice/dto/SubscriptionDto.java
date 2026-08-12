package com.company.chatplatform.paymentservice.dto;

public class SubscriptionDto {
    private String id;
    private String userId;
    private String planName;
    private String status;
    private String expiresAt;
    private String createdAt;

    public SubscriptionDto() {}

    public SubscriptionDto(String id, String userId, String planName, String status, String expiresAt, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.planName = planName;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getStatus() {
        return status;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
