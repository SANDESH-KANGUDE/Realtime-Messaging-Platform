package com.company.chatplatform.chatservice.dto;

public class ChatMemberDto {
    private String id;
    private String chatId;
    private String userId;
    private String role;
    private boolean pinned;
    private String joinedAt;

    public ChatMemberDto() {}

    public ChatMemberDto(String id, String chatId, String userId, String role, boolean pinned, String joinedAt) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
        this.role = role;
        this.pinned = pinned;
        this.joinedAt = joinedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }
}
