# 12_chat-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Manage chat creation, membership, and chat metadata.

---

# Responsibilities

- Direct Chats
- Group Chats
- Chat Members
- Chat Roles
- Chat Settings
- Archive Chats

---

# Database

Tables

- chats
- chat_members
- chat_admins
- user_chat_settings

---

# APIs

POST /chats/direct

POST /chats/group

GET /chats

GET /chats/{id}

PUT /chats/{id}

DELETE /chats/{id}

POST /chats/{id}/members

DELETE /chats/{id}/members/{userId}

---

# Kafka Events

Publish

- chat.created
- group.created
- member.added
- member.removed
- group.updated

Consume

- user.updated
- user.blocked

---

# Business Rules

- One direct chat per user pair
- Only admins manage groups
- Owners cannot leave without transferring ownership
- Blocked users cannot be added

---

# Security

- Members only access chats
- Admin-only group management

---

# Testing

- Direct chat creation
- Group management
- Member operations
- Permissions

---

Status: FINAL