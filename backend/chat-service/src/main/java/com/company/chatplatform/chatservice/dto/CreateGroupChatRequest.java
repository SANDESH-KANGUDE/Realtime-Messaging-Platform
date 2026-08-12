package com.company.chatplatform.chatservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateGroupChatRequest {

    @NotBlank(message = "Group title is required")
    @Size(min = 2, max = 100, message = "Group title must be between 2 and 100 characters")
    private String title;

    private String avatarUrl;

    @NotEmpty(message = "Member user IDs list cannot be empty")
    private List<String> memberUserIds;

    public CreateGroupChatRequest() {}

    public CreateGroupChatRequest(String title, String avatarUrl, List<String> memberUserIds) {
        this.title = title;
        this.avatarUrl = avatarUrl;
        this.memberUserIds = memberUserIds;
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

    public List<String> getMemberUserIds() {
        return memberUserIds;
    }

    public void setMemberUserIds(List<String> memberUserIds) {
        this.memberUserIds = memberUserIds;
    }
}
