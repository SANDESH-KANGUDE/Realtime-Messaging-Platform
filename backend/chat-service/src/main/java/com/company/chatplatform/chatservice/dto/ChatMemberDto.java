package com.company.chatplatform.chatservice.dto;

public class ChatMemberDto {
    private String id;
    private String chatId;
    private String userId;
    private String role;
    private boolean pinned;
    private boolean archived;
    private String theme;
    private String leftAt;
    private boolean active = true;
    private String joinedAt;

    public ChatMemberDto() {}

    public ChatMemberDto(String id, String chatId, String userId, String role, boolean pinned, boolean archived, String theme, String leftAt, boolean active, String joinedAt) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
        this.role = role;
        this.pinned = pinned;
        this.archived = archived;
        this.theme = theme;
        this.leftAt = leftAt;
        this.active = active;
        this.joinedAt = joinedAt;
    }

    public ChatMemberDto(String id, String chatId, String userId, String role, boolean pinned, String joinedAt) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
        this.role = role;
        this.pinned = pinned;
        this.archived = false;
        this.theme = null;
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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(String leftAt) {
        this.leftAt = leftAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
