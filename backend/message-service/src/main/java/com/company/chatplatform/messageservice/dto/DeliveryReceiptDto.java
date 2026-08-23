package com.company.chatplatform.messageservice.dto;

public class DeliveryReceiptDto {
    private String userId;
    private String deliveredAt;

    public DeliveryReceiptDto() {}

    public DeliveryReceiptDto(String userId, String deliveredAt) {
        this.userId = userId;
        this.deliveredAt = deliveredAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(String deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}
