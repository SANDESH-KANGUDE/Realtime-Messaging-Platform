package com.company.chatplatform.adminservice.dto;

public class ModerationReportDto {
    private String id;
    private String reporterId;
    private String reportedUserId;
    private String reason;
    private String status;
    private String createdAt;

    public ModerationReportDto() {}

    public ModerationReportDto(String id, String reporterId, String reportedUserId, String reason, String status, String createdAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
