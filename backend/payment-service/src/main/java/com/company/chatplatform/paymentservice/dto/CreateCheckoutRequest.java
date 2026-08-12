package com.company.chatplatform.paymentservice.dto;

import java.math.BigDecimal;

public class CreateCheckoutRequest {

    private String planId;
    private String planName;
    private BigDecimal amount;
    private String currency = "USD";

    public CreateCheckoutRequest() {}

    public CreateCheckoutRequest(String planName, BigDecimal amount, String currency) {
        this.planName = planName;
        this.amount = amount;
        this.currency = currency != null ? currency : "USD";
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
