package com.company.chatplatform.aiservice.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank(message = "Chat ID is required") String chatId,
        @NotBlank(message = "Message content is required") String content
) {}
