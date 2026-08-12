# 03_Event_Contracts.md

> Version: 1.0
> Status: FINAL
> Last Updated: July 2026
> Audience: Backend Engineers, Platform Engineers, DevOps, AI Coding Agents

---

# Purpose

This document defines every event exchanged between services through Apache Kafka.

It standardizes:

- Topic names
- Event payloads
- Producers
- Consumers
- Versioning
- Ordering
- Retry strategy
- Dead Letter Queue (DLQ)
- Idempotency
- Delivery guarantees

This document is the single source of truth for all Kafka communication.

---

# 1. Why Kafka?

Kafka is used whenever a service should **inform** other services that something happened.

Kafka is **not** used to request information.

Correct example

```
Message Service

↓

Message Sent

↓

Kafka

↓

Notification Service
↓

Search Service
```

Incorrect example

```
Notification Service

↓

Kafka

↓

User Service

↓

Return User Data
```

Use REST for request/response interactions.

---

# 2. Event Naming Convention

Every topic follows

```
<domain>.<event>.v<version>
```

Examples

```
message.sent.v1

message.deleted.v1

chat.group.created.v1

user.profile.updated.v1

payment.completed.v1
```

---

# 3. Event Principles

Every event:

- Represents something that already happened.
- Is immutable.
- Cannot be modified after publishing.
- Can be replayed safely.
- Is versioned.

---

# 4. Standard Event Envelope

Every Kafka message follows the same envelope.

```json
{
  "eventId": "UUIDv7",
  "eventType": "message.sent.v1",
  "occurredAt": "2026-07-20T12:30:00Z",
  "producer": "message-service",
  "correlationId": "UUIDv7",
  "payload": {}
}
```

---

# 5. Event Metadata

| Field | Purpose |
|---------|----------|
| eventId | Unique event identifier |
| eventType | Kafka topic name |
| occurredAt | UTC timestamp |
| producer | Source service |
| correlationId | Request tracing |
| payload | Business data |

---

# 6. Partition Key Strategy

To preserve ordering, every topic uses a deterministic key.

| Domain | Partition Key |
|----------|---------------|
| Message | chatId |
| Chat | chatId |
| User | userId |
| Notification | recipientId |
| Payment | userId |
| Media | mediaId |
| Admin | announcementId |

This guarantees message order within a partition.

---

# 7. Delivery Semantics

Kafka provides

```
At-Least-Once Delivery
```

Consumers must therefore be idempotent.

Duplicate event processing must not create duplicate business actions.

---

# 8. Retry Strategy

Transient failures

↓

Retry

```
1s

5s

15s

30s
```

After retry exhaustion

↓

Dead Letter Queue

---

# 9. Dead Letter Queue

Every domain owns a DLQ.

Examples

```
message.dlq

user.dlq

payment.dlq

notification.dlq
```

DLQs require manual investigation.

---

# 10. Event Versioning

Breaking changes

```
message.sent.v2
```

Never modify

```
message.sent.v1
```

Old consumers continue working.

---

# 11. Auth Service Events

Auth Service intentionally produces **no business events** in v1.

Authentication is handled synchronously through REST.

---

# 12. User Service Events

---

## user.profile.created.v1

Producer

```
User Service
```

Consumers

- Search Service

Payload

```json
{
  "userId": "UUID",
  "displayName": "John",
  "username": "john123"
}
```

---

## user.profile.updated.v1

Consumers

- Search Service

Payload

```json
{
  "userId": "UUID",
  "displayName": "John Smith",
  "profilePicture": "..."
}
```

---

## user.blocked.v1

Consumers

- Chat Service
- Notification Service

Payload

```json
{
  "userId": "UUID",
  "blockedUserId": "UUID"
}
```

---

## user.unblocked.v1

Consumers

- Chat Service

---

# 13. Chat Service Events

---

## chat.direct.created.v1

Consumers

- Search Service

Payload

```json
{
  "chatId": "UUID",
  "members": [
    "UUID",
    "UUID"
  ]
}
```

---

## chat.group.created.v1

Consumers

- Search Service
- Notification Service

Payload

```json
{
  "chatId": "UUID",
  "name": "Backend Team",
  "creatorId": "UUID"
}
```

---

## chat.group.updated.v1

Payload

```json
{
  "chatId": "UUID",
  "name": "Architecture Team"
}
```

---

## chat.member.added.v1

Consumers

- Notification Service
- Realtime Service

Payload

```json
{
  "chatId": "UUID",
  "memberId": "UUID"
}
```

---

## chat.member.removed.v1

Consumers

- Notification Service
- Realtime Service

---

## chat.archived.v1

Consumers

- Search Service

---

# 14. Message Service Events

---

## message.sent.v1

Producer

```
Message Service
```

Consumers

- Notification Service
- Search Service
- Realtime Service

Payload

```json
{
  "messageId": "UUID",
  "chatId": "UUID",
  "senderId": "UUID",
  "sequenceNo": 154,
  "type": "TEXT",
  "content": "Hello"
}
```

---

## message.edited.v1

Payload

```json
{
  "messageId": "UUID",
  "content": "Updated Text"
}
```

---

## message.deleted.v1

Payload

```json
{
  "messageId": "UUID"
}
```

---

## message.read.v1

Consumers

- Notification Service

Payload

```json
{
  "messageId": "UUID",
  "readerId": "UUID"
}
```

---

## message.reaction.added.v1

Payload

```json
{
  "messageId": "UUID",
  "emoji": "❤️",
  "userId": "UUID"
}
```

---

## message.reaction.removed.v1

---

## message.poll.created.v1

Payload

```json
{
  "pollId": "UUID",
  "chatId": "UUID"
}
```

---

## message.poll.voted.v1

Payload

```json
{
  "pollId": "UUID",
  "optionId": "UUID",
  "userId": "UUID"
}
```

---

# 03_Event_Contracts.md

## PART 2

---

# 15. Realtime Service Events

The Realtime Service primarily **consumes** Kafka events.

It does **not** publish business events in v1.

Responsibilities:

- Subscribe to Kafka
- Broadcast via Socket.IO
- Maintain Redis presence
- Handle ACKs
- Handle typing indicators

Consumed Topics

| Topic | Purpose |
|---------|----------|
| message.sent.v1 | Broadcast new message |
| message.edited.v1 | Update message |
| message.deleted.v1 | Remove message |
| message.reaction.added.v1 | Broadcast reaction |
| message.reaction.removed.v1 | Remove reaction |
| message.poll.created.v1 | Broadcast poll |
| message.poll.voted.v1 | Update poll |
| chat.member.added.v1 | Refresh room membership |
| chat.member.removed.v1 | Remove member from room |
| admin.announcement.v1 | Broadcast announcement |

Presence remains inside Redis.

Typing indicators never enter Kafka.

---

# 16. Notification Service Events

Notification Service is almost entirely consumer-driven.

---

## Consumed Topics

| Topic | Action |
|---------|--------|
| message.sent.v1 | Create message notification |
| chat.group.created.v1 | Notify invited members |
| chat.member.added.v1 | Notify added member |
| payment.subscription.activated.v1 | Subscription notification |
| payment.subscription.expired.v1 | Expiration notification |
| admin.announcement.v1 | Platform announcement |

Notification Service does not publish business events in v1.

---

# 17. Media Service Events

Media Service publishes events only after upload completion.

---

## media.upload.completed.v1

Producer

```
Media Service
```

Consumers

Future:

- Analytics
- AI Moderation
- Thumbnail Generator

Payload

```json
{
  "mediaId":"UUID",
  "ownerId":"UUID",
  "mimeType":"image/jpeg",
  "fileSize":248320
}
```

---

## media.deleted.v1

Payload

```json
{
  "mediaId":"UUID"
}
```

Published after permanent cleanup.

---

# 18. Search Service Events

Search Service is a projection service.

It consumes events.

It never publishes business events.

Consumed Topics

```
user.profile.created.v1

user.profile.updated.v1

chat.direct.created.v1

chat.group.created.v1

chat.group.updated.v1

chat.archived.v1

message.sent.v1

message.edited.v1

message.deleted.v1
```

Each event updates the corresponding MongoDB search index.

---

# 19. Admin Service Events

---

## admin.announcement.v1

Producer

```
Admin Service
```

Consumers

- Notification Service
- Realtime Service

Payload

```json
{
  "announcementId":"UUID",
  "title":"Maintenance",
  "message":"Scheduled maintenance at 22:00 UTC"
}
```

---

# 20. Payment Service Events

---

## payment.completed.v1

Producer

```
Payment Service
```

Consumers

Future

- Analytics

Payload

```json
{
  "paymentId":"UUID",
  "userId":"UUID",
  "amount":499.00,
  "currency":"INR"
}
```

---

## payment.subscription.activated.v1

Consumers

- Notification Service

Payload

```json
{
  "subscriptionId":"UUID",
  "userId":"UUID",
  "plan":"PREMIUM_MONTHLY"
}
```

---

## payment.subscription.expired.v1

Consumers

- Notification Service

Payload

```json
{
  "subscriptionId":"UUID",
  "userId":"UUID"
}
```

---

# 21. Producer Matrix

| Service | Produces |
|-----------|----------|
| Auth | None |
| User | 4 |
| Chat | 6 |
| Message | 7 |
| Realtime | None |
| Notification | None |
| Media | 2 |
| Search | None |
| Admin | 1 |
| Payment | 3 |

---

# 22. Consumer Matrix

| Service | Consumes |
|-----------|----------|
| Auth | None |
| User | None |
| Chat | user.blocked.v1, user.unblocked.v1 |
| Message | None |
| Realtime | Message, Chat, Admin events |
| Notification | Message, Chat, Payment, Admin events |
| Media | None |
| Search | User, Chat, Message events |
| Admin | None |
| Payment | None |

---

# 23. Event Flow Examples

---

## Message Sent

```text
Message Service

↓

message.sent.v1

↓

Kafka

↓

Realtime Service
↓

Notification Service
↓

Search Service
```

---

## Group Created

```text
Chat Service

↓

chat.group.created.v1

↓

Kafka

↓

Search Service
↓

Notification Service
```

---

## Subscription Activated

```text
Payment Gateway

↓

Webhook

↓

Payment Service

↓

payment.subscription.activated.v1

↓

Kafka

↓

Notification Service
```

---

# 24. Ordering Guarantees

Ordering is guaranteed **within a partition**.

Partition Keys

| Topic Family | Key |
|---------------|-----|
| Message | chatId |
| Chat | chatId |
| User | userId |
| Payment | userId |
| Media | mediaId |
| Admin | announcementId |

This guarantees:

- Messages within the same chat remain ordered.
- User profile updates remain ordered.
- Subscription lifecycle events remain ordered.

No ordering guarantee exists across different partition keys.

---

# 25. Producer Guidelines

Every producer must:

- Publish only after a successful database transaction.
- Include the standard event envelope.
- Use UUIDv7 event IDs.
- Set the correct partition key.
- Never publish partially completed business actions.

Recommended implementation pattern:

```
Business Transaction

↓

Commit Database

↓

Publish Event
```

For stronger delivery guarantees, the implementation should adopt the **Transactional Outbox Pattern**, where events are written to an outbox table within the same database transaction and published asynchronously by an outbox processor.

---

# 26. Consumer Guidelines

Consumers must:

- Be idempotent.
- Ignore duplicate events.
- Retry transient failures.
- Send unrecoverable failures to the DLQ.
- Never assume event arrival order across different partitions.

Consumers should not call other consumers directly.

---

# 27. Event Choreography

The platform follows **event choreography**, not orchestration.

Example

```
Message Service

↓

Publishes Event

↓

Notification reacts independently

↓

Search reacts independently

↓

Realtime reacts independently
```

There is **no central workflow engine** coordinating these services.

---

# 28. Replay Policy

Because events are immutable:

- Consumers may replay historical events.
- Replays rebuild projections (e.g., Search indexes).
- Business actions must remain idempotent during replay.

Replay should not generate duplicate notifications or duplicate payments.

---

# 29. Dead Letter Queue (DLQ) Policy

Each event domain has a dedicated DLQ.

Examples

```
message.dlq

chat.dlq

payment.dlq

notification.dlq
```

DLQ messages must include:

- Original event
- Failure reason
- Retry count
- Timestamp

DLQs are operational artifacts and should be monitored and drained by platform operations.

---

# 30. Event Governance

Adding a new event requires:

1. Domain ownership confirmation.
2. Topic name review.
3. Payload review.
4. Version review.
5. Consumer impact analysis.
6. Documentation update.
7. Contract testing.

No undocumented Kafka topic may be introduced into the platform.

---

# 31. Event Summary

| Domain | Topics |
|----------|---------|
| User | 4 |
| Chat | 6 |
| Message | 7 |
| Media | 2 |
| Admin | 1 |
| Payment | 3 |
| Auth | 0 |
| Realtime | 0 |
| Notification | 0 |
| Search | 0 |

Approximate total business topics:

**23 versioned Kafka topics**

---

# Final Notes

Kafka is the platform's asynchronous integration backbone.

Business services publish immutable domain events.

Projection services (Search), communication services (Realtime), and delivery services (Notification) react independently, resulting in a loosely coupled, horizontally scalable architecture.

---

**Status:** FINAL

**Version:** 1.0

**Next Document:** `04_Database_Design.md`