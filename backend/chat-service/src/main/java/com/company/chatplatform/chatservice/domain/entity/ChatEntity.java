package com.company.chatplatform.chatservice.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chats")
public class ChatEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "type", nullable = false, length = 20)
    private String type = "DIRECT"; // DIRECT, GROUP

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(name = "created_by", nullable = false, length = 36)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ChatEntity() {}

    public ChatEntity(String id, String type, String title, String avatarUrl, String createdBy) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.avatarUrl = avatarUrl;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
