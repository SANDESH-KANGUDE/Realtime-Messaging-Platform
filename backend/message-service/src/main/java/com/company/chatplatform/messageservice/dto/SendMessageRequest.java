package com.company.chatplatform.messageservice.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class SendMessageRequest {

    @NotBlank(message = "Chat ID is required")
    private String chatId;

    private String content;
    private String type = "TEXT"; // TEXT, IMAGE, FILE, POLL
    private String mediaUrl;
    private String replyToMessageId;
    private List<String> pollOptions;

    public SendMessageRequest() {}

    public SendMessageRequest(String chatId, String content, String type, String mediaUrl, String replyToMessageId) {
        this.chatId = chatId;
        this.content = content;
        this.type = type != null ? type : "TEXT";
        this.mediaUrl = mediaUrl;
        this.replyToMessageId = replyToMessageId;
    }

    public SendMessageRequest(String chatId, String content, String type, String mediaUrl, String replyToMessageId, List<String> pollOptions) {
        this.chatId = chatId;
        this.content = content;
        this.type = type != null ? type : "TEXT";
        this.mediaUrl = mediaUrl;
        this.replyToMessageId = replyToMessageId;
        this.pollOptions = pollOptions;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public List<String> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(List<String> pollOptions) {
        this.pollOptions = pollOptions;
    }
}
