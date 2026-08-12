package com.company.chatplatform.paymentservice.dto;

public class WebhookPayload {

    private String paymentIntentId;
    private String event; // e.g. "payment_intent.succeeded"
    private String paymentId;
    private String status;

    public WebhookPayload() {}

    public WebhookPayload(String paymentIntentId, String event) {
        this.paymentIntentId = paymentIntentId;
        this.event = event;
    }

    public String getPaymentIntentId() {
        return paymentIntentId != null ? paymentIntentId : paymentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getEvent() {
        if (event != null) {
            return event;
        }
        if ("COMPLETED".equalsIgnoreCase(status)) {
            return "payment_intent.succeeded";
        }
        return status;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
