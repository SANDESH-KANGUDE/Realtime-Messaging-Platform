package com.company.chatplatform.adminservice.dto;

import jakarta.validation.constraints.NotBlank;

public class BanUserRequest {

    @NotBlank(message = "Target User ID is required")
    private String targetUserId;

    @NotBlank(message = "Reason is required")
    private String reason;

    public BanUserRequest() {}

    public BanUserRequest(String targetUserId, String reason) {
        this.targetUserId = targetUserId;
        this.reason = reason;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
