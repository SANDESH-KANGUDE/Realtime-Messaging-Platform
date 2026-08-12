package com.company.chatplatform.adminservice.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "moderation_reports")
public class ModerationReportEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "reporter_id", nullable = false, length = 36)
    private String reporterId;

    @Column(name = "reported_user_id", nullable = false, length = 36)
    private String reportedUserId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "OPEN"; // OPEN, RESOLVED, DISMISSED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ModerationReportEntity() {}

    public ModerationReportEntity(String id, String reporterId, String reportedUserId, String reason) {
        this.id = id;
        this.reporterId = reporterId;
        this.reportedUserId = reportedUserId;
        this.reason = reason;
        this.status = "OPEN";
        this.createdAt = Instant.now();
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

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
