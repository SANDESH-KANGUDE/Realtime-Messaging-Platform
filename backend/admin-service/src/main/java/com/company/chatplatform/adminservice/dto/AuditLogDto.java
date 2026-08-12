package com.company.chatplatform.adminservice.dto;

public class AuditLogDto {
    private String id;
    private String adminId;
    private String action;
    private String targetId;
    private String details;
    private String createdAt;

    public AuditLogDto() {}

    public AuditLogDto(String id, String adminId, String action, String targetId, String details, String createdAt) {
        this.id = id;
        this.adminId = adminId;
        this.action = action;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getAdminId() {
        return adminId;
    }

    public String getAction() {
        return action;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getDetails() {
        return details;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
