package com.company.chatplatform.adminservice.dto;

import jakarta.validation.constraints.NotBlank;

public class ReportUserRequest {

    @NotBlank(message = "Reported User ID is required")
    private String reportedUserId;

    @NotBlank(message = "Reason is required")
    private String reason;

    public ReportUserRequest() {}

    public ReportUserRequest(String reportedUserId, String reason) {
        this.reportedUserId = reportedUserId;
        this.reason = reason;
    }

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
