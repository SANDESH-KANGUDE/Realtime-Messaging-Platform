package com.company.chatplatform.messageservice.domain.document;

import java.time.Instant;

public class DeliveryReceiptDocument {
    private String userId;
    private Instant deliveredAt = Instant.now();

    public DeliveryReceiptDocument() {}

    public DeliveryReceiptDocument(String userId) {
        this.userId = userId;
        this.deliveredAt = Instant.now();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}
