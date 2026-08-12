# 0001. Microservices Architecture

- **Status**: Accepted
- **Date**: 2026-07-21

## Context
The platform requires high scalability, domain isolation, independent deployability, and maintainability for a multi-tenant real-time chat application with diverse workloads (messaging, presence, search, payments, media).

## Decision
We adopt a microservices architecture guided by Domain-Driven Design (DDD) and the Database-Per-Service principle. Each service owns exactly one business domain and one database.

## Consequences
- **Positive**: Loose coupling, targeted scaling, fault isolation.
- **Negative**: Requires distributed tracing, central API Gateway, transactional outbox for async events.
