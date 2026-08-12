# 18_admin-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Administrative and moderation operations.

---

# Responsibilities

- User Moderation
- Audit Logs
- Reports
- System Metrics

---

# Database

Tables

- admins
- audit_logs

---

# APIs

GET /admin/users

PUT /admin/users/{id}

GET /admin/reports

GET /admin/audit

---

# Kafka Events

Consume

- user.updated
- payment.completed

---

# Business Rules

- RBAC enforced
- Immutable audit logs

---

# Security

- Admin-only access
- MFA recommended

---

# Testing

- Authorization
- Audit logging
- Moderation

---

Status: FINAL