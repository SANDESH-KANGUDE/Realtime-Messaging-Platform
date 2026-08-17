package com.company.chatplatform.messageservice.dto;

public class ReadReceiptDto {
    private String userId;
    private String readAt;

    public ReadReceiptDto() {}

    public ReadReceiptDto(String userId, String readAt) {
        this.userId = userId;
        this.readAt = readAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReadAt() {
        return readAt;
    }

    public void setReadAt(String readAt) {
        this.readAt = readAt;
    }
}
