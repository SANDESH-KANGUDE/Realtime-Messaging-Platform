# 0004. MongoDB with Dedicated Outbox Collection

- **Status**: Accepted
- **Date**: 2026-07-21

## Context
Message Service stores messages and metadata in MongoDB for high throughput, flexible document structure, and horizontal scaling. However, event publishing to Kafka must be reliable and atomic.

## Decision
Message Service utilizes a dedicated `outbox_messages` MongoDB collection alongside a background polling worker to publish events to Kafka (Transactional Outbox Pattern for NoSQL).

## Consequences
- **Positive**: Guaranteed at-least-once event publishing without relying on multi-document ACID transactions across distributed databases.
- **Negative**: Requires periodic outbox table cleanup.
