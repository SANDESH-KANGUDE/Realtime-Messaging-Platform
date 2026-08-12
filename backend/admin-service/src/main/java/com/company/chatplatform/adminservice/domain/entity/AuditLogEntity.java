package com.company.chatplatform.adminservice.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "admin_id", nullable = false, length = 36)
    private String adminId;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "target_id", nullable = false, length = 36)
    private String targetId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AuditLogEntity() {}

    public AuditLogEntity(String id, String adminId, String action, String targetId, String details) {
        this.id = id;
        this.adminId = adminId;
        this.action = action;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = Instant.now();
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
