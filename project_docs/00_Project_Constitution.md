# 00_Project_Constitution.md

> Version: 1.0
> Last Updated: July 2026
> Status: FINAL
> Audience: Architects, Backend Engineers, Frontend Engineers, DevOps Engineers, QA Engineers, AI Coding Agents

---

# 1. Purpose

This document defines the constitutional rules of the Chat Platform.

Every architectural decision, implementation detail, code contribution, and future enhancement MUST comply with the rules defined in this document.

If any future implementation contradicts this constitution, the constitution takes precedence.

This document acts as the single source of truth for all engineering teams and AI-assisted development tools.

---

# 2. Project Vision

Build a production-grade, cloud-native, event-driven messaging platform inspired by modern real-time communication systems such as WhatsApp, Discord, Telegram, and Slack.

The platform prioritizes:

- Scalability
- Reliability
- Maintainability
- Loose Coupling
- Independent Deployability
- Domain Ownership
- Clean Architecture
- Security
- Extensibility

The objective is not merely to build a chat application, but to establish a software architecture capable of evolving into a large-scale communication platform.

---

# 3. Technology Stack

## Backend

- Java 21
- Spring Boot
- Spring Cloud
- Spring Cloud Gateway
- Spring Security
- Spring Data JPA
- Spring Data MongoDB
- Socket.IO Java Server

## Frontend

- React
- TypeScript
- Vite

## Databases

- PostgreSQL
- MongoDB
- Redis

## Messaging

- Apache Kafka

## Storage

- S3-Compatible Object Storage

Examples:

- AWS S3
- MinIO
- DigitalOcean Spaces

## Infrastructure

- Docker
- Kubernetes
- NGINX Ingress Controller

## Build

- Maven

---

# 4. Architectural Style

The system follows:

- Microservices Architecture
- Event-Driven Architecture
- Domain-Driven Design (DDD)
- Clean Architecture
- API First Design
- CQRS where appropriate

Every service owns exactly one business domain.

---

# 5. Domain Ownership Principle

Every business entity has exactly one owner.

No business entity shall have multiple owners.

Current ownership:

| Domain | Owner |
|----------|----------------|
| Authentication | Auth Service |
| User Profile | User Service |
| User Preferences | User Service |
| Friend Graph | User Service |
| Chat Metadata | Chat Service |
| Group Management | Chat Service |
| Messages | Message Service |
| Reactions | Message Service |
| Read Receipts | Message Service |
| Presence | Realtime Service |
| Notifications | Notification Service |
| Media Files | Media Service |
| Search Index | Search Service |
| Administration | Admin Service |
| Payments | Payment Service |

---

# 6. Database Ownership Rule

Each service owns its own database.

Services SHALL NEVER directly access another service's database.

Forbidden:

```
Message Service

↓

User Database
```

Allowed:

```
Message Service

↓

REST

↓

User Service
```

or

```
Message Service

↓

Kafka Event
```

---

# 7. Communication Rules

## Synchronous Communication

REST APIs

Used when immediate response is required.

Examples:

- Login
- Fetch Profile
- Create Chat
- Send Message
- Upload Metadata

---

## Asynchronous Communication

Kafka

Used for:

- Notifications
- Search Index Updates
- Analytics
- Event Propagation
- Future Integrations

No service should wait for Kafka consumers to complete.

---

# 8. API Design Standards

All REST endpoints MUST follow:

```
/api/v1/
```

Example:

```
/api/v1/messages
```

Versioning is mandatory.

Future versions:

```
/api/v2/
```

must coexist without breaking clients.

---

# 9. Identifier Strategy

All primary identifiers use:

UUIDv7

Reasons:

- Globally unique
- Time sortable
- Database friendly
- Distributed generation

Auto-increment IDs are prohibited.

---

# 10. Authentication

Authentication uses JWT.

REST:

```
Authorization: Bearer <token>
```

Realtime:

JWT exchanged during Socket.IO handshake.

No session-based authentication.

---

# 11. Authorization

Every service validates authorization independently.

Gateway authentication DOES NOT replace service-level authorization.

Every service is responsible for verifying:

- Ownership
- Membership
- Roles
- Permissions

---

# 12. DTO Rule

Services NEVER expose entities directly.

Only DTOs cross service boundaries.

Internal models remain private.

---

# 13. Event Naming Convention

Kafka topics follow:

```
domain.event.v1
```

Examples:

```
message.sent.v1

message.edited.v1

chat.created.v1

user.profile.updated.v1
```

Breaking changes create:

```
message.sent.v2
```

Never modify existing payloads.

---

# 14. Error Handling

Every service returns standardized error responses.

Example:

```json
{
  "timestamp": "...",
  "status": 404,
  "code": "USER_001",
  "message": "User not found",
  "path": "/api/v1/users/123"
}
```

Every service owns its own error codes.

---

# 15. Soft Delete Policy

Where business history matters, entities are soft deleted.

Examples:

- Messages
- Groups
- Media References

Hard deletion is reserved for cleanup jobs.

---

# 16. Stateless Services

Every service must remain stateless.

State belongs in:

- PostgreSQL
- MongoDB
- Redis
- Kafka

Never application memory.

---

# 17. Configuration Management

Configuration must be externalized.

Never hardcode:

- Secrets
- URLs
- Credentials
- Tokens

Use:

- application.yml
- Environment Variables
- Kubernetes Secrets

---

# 18. Logging

Every service logs:

- Requests
- Errors
- Warnings
- Kafka Events
- Startup Information

Never log:

- Passwords
- JWT Tokens
- Sensitive Personal Data

---

# 19. Observability

The architecture reserves support for:

- Prometheus
- Grafana
- Centralized Logging
- Distributed Tracing

Analytics is intentionally separated from business services.

---

# 20. Testing Philosophy

Every service should support:

- Unit Tests
- Integration Tests
- Contract Tests
- End-to-End Tests

Testing is mandatory before deployment.

---

# 21. Security Principles

- Principle of Least Privilege
- HTTPS Everywhere
- JWT Authentication
- Service-to-Service Authentication
- Input Validation
- Output Sanitization
- Secure File Uploads
- No Trust in Client Data

Webhook verification is mandatory for payment confirmation.

---

# 22. Scalability Principles

Every service should scale independently.

No service should require another service to scale.

Services remain horizontally scalable.

---

# 23. AI Development Rules

AI coding agents (Antigravity IDE, Codex, Claude Code, Gemini CLI, etc.) MUST:

1. Respect service ownership.
2. Never create shared databases.
3. Never bypass APIs.
4. Never bypass Kafka for asynchronous workflows.
5. Follow folder structure exactly.
6. Generate DTOs instead of exposing entities.
7. Follow coding standards.
8. Preserve API contracts.
9. Preserve event contracts.
10. Preserve backward compatibility.

---

# 24. Definition of Done

A service is considered complete only if it includes:

- Domain implementation
- REST APIs
- DTOs
- Validation
- Authorization
- Database schema
- Kafka integration
- Error handling
- Logging
- Unit tests
- Docker support
- OpenAPI documentation

---

# 25. Final Constitutional Statement

This constitution governs the entire platform.

All future architectural decisions, implementation work, AI-generated code, and engineering contributions must comply with these principles.

Any deviation from this document requires an explicit architectural review and version update.

---

**Status:** FINAL

**Version:** 1.0

**Next Document:** 01_System_Architecture.md