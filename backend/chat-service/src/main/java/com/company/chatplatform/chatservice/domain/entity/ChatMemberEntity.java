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

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    public ChatMemberEntity() {}

    public ChatMemberEntity(String id, String chatId, String userId, String role) {
        this.id = id;
        this.chatId = chatId;
        this.userId = userId;
        this.role = role != null ? role : "MEMBER";
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

    public Instant getJoinedAt() {
        return joinedAt;
    }
}
