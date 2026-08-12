package com.company.chatplatform.chatservice.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateDirectChatRequest {

    @NotBlank(message = "Target User ID is required")
    private String targetUserId;

    public CreateDirectChatRequest() {}

    public CreateDirectChatRequest(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }
}
