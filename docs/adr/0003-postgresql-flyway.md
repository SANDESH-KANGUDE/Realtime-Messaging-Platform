# 0003. PostgreSQL with Flyway Migrations

- **Status**: Accepted
- **Date**: 2026-07-21

## Context
Relational services (Auth, User, Chat, Notification, Media, Admin, Payment) require transactional consistency and predictable schema evolution.

## Decision
We mandate PostgreSQL 16 for relational stores and Flyway for versioned database migration scripts (`V1__*.sql`).

## Consequences
- **Positive**: Strict schema version control, automated migration on startup, reproducible environments.
- **Negative**: DDL changes must be backward-compatible.
