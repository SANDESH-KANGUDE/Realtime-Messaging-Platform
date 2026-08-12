# 17_search-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Provide fast search capabilities.

---

# Responsibilities

- User Search
- Chat Search
- Message Search

---

# Database

Collections

- user_index
- chat_index
- message_index

---

# APIs

GET /search/users

GET /search/chats

GET /search/messages

---

# Kafka Events

Consume

- user.updated
- message.sent
- chat.created

---

# Business Rules

- Eventual consistency
- Index rebuild support

---

# Security

- Return only authorized results

---

# Testing

- Index updates
- Search accuracy
- Permissions

---

Status: FINAL