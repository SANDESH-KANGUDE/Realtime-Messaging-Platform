# 11_user-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Manage user profiles and social relationships.

---

# Responsibilities

- Profile Management
- Friend Requests
- Friend List
- Blocking Users
- User Preferences

---

# Database

Tables

- users
- friend_requests
- friendships
- blocked_users
- user_preferences

---

# APIs

GET /users/{id}

PUT /users/profile

POST /friends/request

PUT /friends/{id}/accept

DELETE /friends/{id}

POST /users/block

DELETE /users/block

GET /users/search

---

# Kafka Events

Publish

- user.updated
- friend.requested
- friend.accepted
- user.blocked

Consume

- auth.user.registered

---

# Business Rules

- Users cannot friend themselves
- Duplicate requests not allowed
- Blocked users cannot interact
- Username uniqueness enforced

---

# Security

- JWT Required
- Users modify only their own profiles
- Admin privileges for moderation

---

# Testing

- Profile CRUD
- Friend Workflow
- Blocking
- Search
- Preferences

---

Status: FINAL