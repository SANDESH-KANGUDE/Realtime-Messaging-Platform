package com.company.chatplatform.userservice.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blocked_users")
public class BlockedUser {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "blocker_id", nullable = false, length = 36)
    private String blockerId;

    @Column(name = "blocked_id", nullable = false, length = 36)
    private String blockedId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public BlockedUser() {}

    public BlockedUser(String id, String blockerId, String blockedId) {
        this.id = id;
        this.blockerId = blockerId;
        this.blockedId = blockedId;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getBlockerId() {
        return blockerId;
    }

    public String getBlockedId() {
        return blockedId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
