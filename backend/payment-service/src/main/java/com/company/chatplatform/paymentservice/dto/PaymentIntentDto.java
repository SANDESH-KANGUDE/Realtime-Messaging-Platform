package com.company.chatplatform.paymentservice.dto;

import java.math.BigDecimal;

public class PaymentIntentDto {
    private String id;
    private String userId;
    private String planName;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String createdAt;

    public PaymentIntentDto() {}

    public PaymentIntentDto(String id, String userId, String planName, BigDecimal amount, String currency, String status, String createdAt) {
        this.id = id;
        this.userId = userId;
        this.planName = planName;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
