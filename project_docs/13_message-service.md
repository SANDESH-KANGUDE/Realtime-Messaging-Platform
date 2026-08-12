# 13_message-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Handle all messaging functionality.

---

# Responsibilities

- Send Messages
- Edit Messages
- Delete Messages
- Reactions
- Read Receipts
- Polls
- Message Search Metadata

---

# Database

MongoDB

- messages
- reactions
- receipts
- polls
- poll_votes

---

# APIs

POST /messages

PUT /messages/{id}

DELETE /messages/{id}

GET /messages/chat/{chatId}

POST /messages/{id}/reaction

DELETE /messages/{id}/reaction

POST /messages/{id}/read

---

# Kafka Events

Publish

- message.sent
- message.edited
- message.deleted
- reaction.added
- reaction.removed
- message.read

Consume

- chat.created
- member.removed

---

# Business Rules

- Only sender edits/deletes
- Edit window configurable
- Soft delete only
- Reactions unique per user
- Read receipts monotonic

---

# Security

- Chat members only
- Media ownership validation

---

# Testing

- CRUD
- Pagination
- Reactions
- Read receipts
- Poll voting

---

Status: FINAL