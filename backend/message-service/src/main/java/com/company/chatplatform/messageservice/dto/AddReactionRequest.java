package com.company.chatplatform.messageservice.dto;

import jakarta.validation.constraints.NotBlank;

public class AddReactionRequest {

    @NotBlank(message = "Emoji is required")
    private String emoji;

    public AddReactionRequest() {}

    public AddReactionRequest(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }
}
