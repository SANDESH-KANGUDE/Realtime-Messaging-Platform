package com.company.chatplatform.chatservice.dto;

import java.util.List;

public class ChatDto {
    private String id;
    private String type;
    private String title;
    private String avatarUrl;
    private String createdBy;
    private List<ChatMemberDto> members;
    private boolean pinned;
    private String createdAt;
    private String updatedAt;

    public ChatDto() {}

    public ChatDto(String id, String type, String title, String avatarUrl, String createdBy, List<ChatMemberDto> members, boolean pinned, String createdAt, String updatedAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.avatarUrl = avatarUrl;
        this.createdBy = createdBy;
        this.members = members;
        this.pinned = pinned;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public List<ChatMemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<ChatMemberDto> members) {
        this.members = members;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
