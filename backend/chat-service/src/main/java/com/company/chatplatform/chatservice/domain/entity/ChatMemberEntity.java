package com.company.chatplatform.chatservice.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_members")
public class ChatMemberEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "chat_id", nullable = false, length = 36)
    private String chatId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "role", nullable = false, length = 20)
    private String role = "MEMBER"; // OWNER, ADMIN, MEMBER

    @Column(name = "pinned", nullable = false)
    private boolean pinned = false;

    @Column(name = "archived", nullable = false)
    private boolean archived = false;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    @Column(name = "theme", length = 50)
    private String theme;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public ChatMemberEntity() {}

    public ChatMemberEntity(String id, String chatId, String userId, String role) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
        this.role = role != null ? role : "MEMBER";
        this.pinned = false;
        this.archived = false;
        this.active = true;
        this.joinedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }

    public String getUserId() {
        return userId;
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

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
