# 15_notification-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Deliver in-app and push notifications.

---

# Responsibilities

- Notification Storage
- Push Delivery
- Read Status
- Notification Preferences

---

# Database

Table

- notifications

---

# APIs

GET /notifications

PUT /notifications/{id}/read

DELETE /notifications/{id}

---

# Kafka Events

Consume

- message.sent
- friend.requested
- payment.completed

Publish

- notification.created

---

# Business Rules

- Respect notification preferences
- Deduplicate repeated notifications

---

# Security

- Owner-only access

---

# Testing

- Delivery
- Read status
- Preferences

---

Status: FINAL