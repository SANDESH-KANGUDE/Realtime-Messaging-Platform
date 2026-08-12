# 04_Database_Design.md

## PART 1 — Relational Database Design

> Version: 1.0
> Status: FINAL
> Database: PostgreSQL
> Audience: Backend Engineers, Database Engineers, DevOps, AI Coding Agents

---

# 1. Purpose

This document defines the persistence layer for the Chat Platform.

It specifies:

- Database ownership
- Schema design
- Relationships
- Constraints
- Indexes
- Naming conventions
- Lifecycle policies
- Performance recommendations

Every service owns its own database schema.

No service may directly query another service's tables.

---

# 2. Database Philosophy

The platform follows the **Database per Service** pattern.

```
             +----------------+
             | User Service   |
             +----------------+
                    │
              PostgreSQL
                    │
             users, friendships
                    │

             +----------------+
             | Chat Service   |
             +----------------+
                    │
              PostgreSQL
                    │
         chats, memberships
```

Cross-service communication is performed through:

- REST APIs
- Kafka Events

Never through SQL joins.

---

# 3. PostgreSQL Standards

## Primary Keys

Every table uses:

```
UUIDv7
```

Example

```
id UUID PRIMARY KEY
```

Advantages

- Globally unique
- Time ordered
- Distributed generation
- No sequence bottlenecks

---

## Timestamp Columns

Every table contains

```sql
created_at TIMESTAMP WITH TIME ZONE NOT NULL

updated_at TIMESTAMP WITH TIME ZONE NOT NULL
```

Soft-deletable tables additionally contain

```sql
deleted_at TIMESTAMP WITH TIME ZONE
```

---

## Naming Convention

### Tables

Plural nouns

```
users

friendships

group_members
```

---

### Columns

Snake case

```
display_name

created_at

updated_at
```

---

### Foreign Keys

```
user_id

chat_id

group_id
```

---

### Indexes

```
idx_users_email

idx_friendships_receiver

idx_chat_members_chat
```

---

## Enumerations

Prefer PostgreSQL ENUMs (or lookup tables if future extensibility is expected).

Example

```
ACTIVE

BLOCKED

SUSPENDED
```

---

# 4. Auth Service Database

Owner

```
Auth Service
```

Database

```
auth_db
```

Tables

```
credentials

refresh_tokens
```

---

# credentials

Purpose

Stores authentication credentials.

User profile information is NOT stored here.

---

Columns

| Column | Type | Notes |
|----------|------|-------|
| id | UUID | PK |
| user_id | UUID | References User Service logically (no FK) |
| email | VARCHAR(255) | Unique |
| phone | VARCHAR(20) | Unique |
| password_hash | TEXT | BCrypt/Argon2 hash |
| status | ENUM | ACTIVE, LOCKED |
| created_at | TIMESTAMPTZ | Required |
| updated_at | TIMESTAMPTZ | Required |

---

Indexes

```
UNIQUE(email)

UNIQUE(phone)

INDEX(status)
```

---

Constraints

- Email unique
- Phone unique
- Password hash never null

---

Lifecycle

Created

↓

Updated

↓

Disabled (optional)

Never hard deleted in normal operation.

---

Sample Row

| id | email | phone | status |
|----|--------|--------|--------|
| UUID | john@example.com | +919876543210 | ACTIVE |

---

Design Rationale

Authentication data is isolated from profile data to minimize exposure of sensitive information and simplify security auditing.

---

# refresh_tokens

Purpose

Stores active refresh tokens.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| user_id | UUID |
| token_hash | TEXT |
| expires_at | TIMESTAMPTZ |
| revoked | BOOLEAN |
| created_at | TIMESTAMPTZ |

---

Indexes

```
INDEX(user_id)

INDEX(expires_at)
```

---

Lifecycle

Created on login.

Revoked on logout.

Expired tokens cleaned by scheduled job.

---

# 5. User Service Database

Owner

```
User Service
```

Database

```
user_db
```

Tables

```
users

friend_requests

friendships

blocked_users

user_preferences
```

---

# users

Purpose

Stores user profile information.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| username | VARCHAR(50) |
| display_name | VARCHAR(100) |
| bio | TEXT |
| profile_picture | TEXT |
| status | ENUM |
| created_at | TIMESTAMPTZ |
| updated_at | TIMESTAMPTZ |

---

Indexes

```
UNIQUE(username)

INDEX(display_name)

INDEX(status)
```

---

Constraints

- Username unique
- Display name required
- Username immutable after creation (recommended)

---

Design Rationale

Authentication credentials are intentionally excluded.

---

# friend_requests

Purpose

Stores pending friendship invitations.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| sender_id | UUID |
| receiver_id | UUID |
| status | ENUM |
| created_at | TIMESTAMPTZ |

---

Status

```
PENDING

ACCEPTED

REJECTED

CANCELLED
```

---

Indexes

```
INDEX(sender_id)

INDEX(receiver_id)

INDEX(status)
```

---

Business Rules

- One active request per pair.
- Cannot send to yourself.
- Cannot send if blocked.

---

# friendships

Purpose

Represents accepted friendships.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| user_id | UUID |
| friend_id | UUID |
| created_at | TIMESTAMPTZ |

---

Indexes

```
INDEX(user_id)

INDEX(friend_id)
```

---

Business Rules

Friendship is symmetric.

Recommended implementation:

Store **two rows**.

```
A → B

B → A
```

Advantages

- Simpler queries
- Faster lookups
- No UNION operations

---

# blocked_users

Purpose

Stores block relationships.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| blocker_id | UUID |
| blocked_user_id | UUID |
| created_at | TIMESTAMPTZ |

---

Indexes

```
INDEX(blocker_id)

INDEX(blocked_user_id)
```

---

Business Rules

Blocking prevents:

- Friend requests
- New direct chats
- Notifications (where applicable)

---

# user_preferences

Purpose

Stores configurable user settings.

---

Columns

| Column | Type |
|----------|------|
| user_id | UUID |
| notifications_enabled | BOOLEAN |
| dark_mode | BOOLEAN |
| language | VARCHAR(10) |
| timezone | VARCHAR(50) |
| created_at | TIMESTAMPTZ |
| updated_at | TIMESTAMPTZ |

---

Relationship

```
1 User

↓

1 Preferences
```

---

# 6. Chat Service Database

Owner

```
Chat Service
```

Database

```
chat_db
```

Tables

```
chats

chat_members

chat_admins

user_chat_settings
```

---

# chats

Purpose

Stores metadata for both direct and group chats.

Messages are NOT stored here.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| type | ENUM |
| name | VARCHAR(150) |
| description | TEXT |
| creator_id | UUID |
| status | ENUM |
| allow_member_invites | BOOLEAN |
| created_at | TIMESTAMPTZ |
| updated_at | TIMESTAMPTZ |
| deleted_at | TIMESTAMPTZ |

---

Type

```
DIRECT

GROUP

SELF
```

---

Status

```
ACTIVE

ARCHIVED

DELETED
```

---

Indexes

```
INDEX(type)

INDEX(status)

INDEX(creator_id)
```

---

Business Rules

- DIRECT chats contain exactly two members.
- SELF chats contain exactly one member.
- GROUP chats contain two or more members.
- Soft delete only.

---

# chat_members

Purpose

Stores chat membership.

---

Columns

| Column | Type |
|----------|------|
| chat_id | UUID |
| user_id | UUID |
| joined_at | TIMESTAMPTZ |

Composite Primary Key

```
(chat_id, user_id)
```

---

Indexes

```
INDEX(user_id)

INDEX(chat_id)
```

---

Business Rules

One membership per user per chat.

---

# chat_admins

Purpose

Stores administrator privileges.

Applicable only to group chats.

---

Columns

| Column | Type |
|----------|------|
| chat_id | UUID |
| user_id | UUID |
| granted_at | TIMESTAMPTZ |

Composite Primary Key

```
(chat_id, user_id)
```

---

Business Rules

Every group has at least one administrator.

---

# user_chat_settings

Purpose

Stores per-user conversation preferences.

These settings are **personal** and do not affect other participants.

---

Columns

| Column | Type |
|----------|------|
| chat_id | UUID |
| user_id | UUID |
| pinned | BOOLEAN |
| archived | BOOLEAN |
| muted_until | TIMESTAMPTZ |
| created_at | TIMESTAMPTZ |
| updated_at | TIMESTAMPTZ |

Composite Primary Key

```
(chat_id, user_id)
```

---

Indexes

```
INDEX(user_id)

INDEX(chat_id)

INDEX(archived)

INDEX(pinned)
```

---

Design Rationale

Separating user-specific settings from the `chats` table avoids duplication and allows every participant to maintain independent preferences.

---

# Summary

| Service | Database | Tables |
|----------|----------|--------|
| Auth | auth_db | 2 |
| User | user_db | 5 |
| Chat | chat_db | 4 |

Total relational tables documented in Part 1: **11**

---

**End of Part 1**

**Next Part**

- Message Service (MongoDB)
- Search Service (MongoDB)
- Realtime Service (Redis)

# 7. Message Service Database

Owner

```
Message Service
```

Database

```
message_db
```

Technology

```
MongoDB
```

Collections

```
messages

message_reactions

message_receipts

polls

poll_votes
```

---

# Why MongoDB?

Messages are:

- Write intensive
- Read intensive
- Variable in structure
- Frequently paginated
- Rarely joined

MongoDB provides:

- High write throughput
- Flexible documents
- Horizontal scaling
- Efficient pagination
- Natural JSON representation

---

# messages

Purpose

Stores every message exchanged on the platform.

---

Document Structure

```json
{
  "_id":"UUID",

  "chatId":"UUID",

  "sequenceNo":154,

  "senderId":"UUID",

  "type":"TEXT",

  "content":"Hello World",

  "mediaId":null,

  "replyTo":null,

  "edited":false,

  "deleted":false,

  "createdAt":"...",

  "updatedAt":"..."
}
```

---

Fields

| Field | Type | Notes |
|---------|------|---------|
| _id | UUID | Primary Key |
| chatId | UUID | Conversation |
| sequenceNo | Long | Server generated |
| senderId | UUID | Sender |
| type | Enum | TEXT, IMAGE, VIDEO... |
| content | String | Nullable for media |
| mediaId | UUID | Optional |
| replyTo | UUID | Optional |
| edited | Boolean | Edit flag |
| deleted | Boolean | Soft delete |
| createdAt | Date | Required |
| updatedAt | Date | Required |

---

Indexes

```text
(chatId, sequenceNo)

senderId

createdAt

mediaId
```

---

Business Rules

- sequenceNo strictly increases within a chat.
- Never physically delete.
- Media stored separately.
- Reply references another message.

---

Lifecycle

Create

↓

Edit

↓

Soft Delete

↓

Archived forever

---

Example

```json
{
 "_id":"...",
 "chatId":"...",
 "sequenceNo":55,
 "senderId":"...",
 "type":"TEXT",
 "content":"Good Morning",
 "edited":false,
 "deleted":false
}
```

---

Performance Notes

Primary reads use

```
chatId + sequenceNo
```

This makes infinite scrolling extremely efficient.

---

# message_reactions

Purpose

Stores reactions independently.

Avoids growing message documents.

---

Document

```json
{
 "_id":"UUID",

 "messageId":"UUID",

 "userId":"UUID",

 "emoji":"❤️",

 "createdAt":"..."
}
```

---

Indexes

```text
messageId

(messageId,userId)

userId
```

---

Business Rules

One user

↓

One reaction

↓

Per emoji

---

# message_receipts

Purpose

Tracks delivery and read state.

---

Document

```json
{
 "_id":"UUID",

 "messageId":"UUID",

 "userId":"UUID",

 "delivered":true,

 "deliveredAt":"...",

 "read":true,

 "readAt":"..."
}
```

---

Indexes

```text
messageId

(messageId,userId)

userId
```

---

Why combine delivery and read?

They represent a sequential lifecycle.

```
SENT

↓

DELIVERED

↓

READ
```

Combining them reduces lookups.

---

# polls

Purpose

Stores poll metadata.

---

Document

```json
{
 "_id":"UUID",

 "chatId":"UUID",

 "question":"Best Database?",

 "options":[

{

"id":"1",

"text":"MongoDB"

},

{

"id":"2",

"text":"PostgreSQL"

}

],

 "closed":false,

 "createdBy":"UUID",

 "createdAt":"..."
}
```

---

Indexes

```text
chatId

createdBy
```

---

Business Rules

Options become immutable after creation.

---

# poll_votes

Purpose

Stores votes separately.

---

Document

```json
{
 "_id":"UUID",

 "pollId":"UUID",

 "optionId":"UUID",

 "userId":"UUID",

 "votedAt":"..."
}
```

---

Indexes

```text
pollId

(pollId,userId)

userId
```

---

Business Rules

One vote

↓

Per poll

↓

Per user

---

# Collection Summary

| Collection | Purpose |
|------------|----------|
| messages | Core Messages |
| message_reactions | Emoji Reactions |
| message_receipts | Delivery & Read |
| polls | Poll Metadata |
| poll_votes | Poll Votes |

---

# 8. Search Service Database

Owner

```
Search Service
```

Database

```
search_db
```

Technology

```
MongoDB
```

Collections

```
user_index

chat_index

message_index
```

---

Purpose

Search owns projections.

Never source data.

---

# user_index

Document

```json
{
 "_id":"UUID",

 "displayName":"John",

 "username":"john123",

 "profilePicture":"..."
}
```

---

Indexes

```text
TEXT(displayName)

TEXT(username)
```

---

# chat_index

Document

```json
{
 "_id":"UUID",

 "name":"Backend Team",

 "type":"GROUP"
}
```

---

Indexes

```text
TEXT(name)
```

---

# message_index

Document

```json
{
 "_id":"UUID",

 "chatId":"UUID",

 "content":"Spring Boot",

 "deleted":false
}
```

---

Indexes

```text
TEXT(content)

chatId
```

---

Update Flow

```
Kafka Event

↓

Update Projection

↓

MongoDB
```

---

# Why Separate Collections?

Independent indexes.

Simpler updates.

Smaller documents.

Better search performance.

---

# 9. Realtime Service Database

Owner

```
Realtime Service
```

Technology

```
Redis
```

Redis stores

```
Presence

Connections

Socket Mapping
```

Never persistent business data.

---

# Key

```
presence:{userId}
```

Value

```json
{
 "status":"ONLINE",

 "lastHeartbeat":"...",

 "deviceCount":2
}
```

TTL

```
None

Updated every heartbeat
```

---

# Key

```
connections:{userId}
```

Value

```text
socket123

socket456
```

Redis Type

```
SET
```

Allows multiple devices.

---

# Key

```
socket:{socketId}
```

Value

```json
{
 "userId":"UUID",

 "connectedAt":"..."
}
```

---

# Why Redis?

Presence changes constantly.

Typical workload

```
ONLINE

↓

OFFLINE

↓

ONLINE

↓

OFFLINE
```

Thousands of updates

↓

Per second

Redis excels at this.

---

# Redis Summary

| Key | Type |
|---------|--------|
| presence:{userId} | JSON/String |
| connections:{userId} | Set |
| socket:{socketId} | JSON/String |

---

# Design Decisions

MongoDB

✔ Flexible Schema

✔ High Write Throughput

✔ Efficient Pagination

✔ Easy Horizontal Scaling

Redis

✔ Memory Speed

✔ Presence Tracking

✔ Socket Mapping

✔ Multi-device Support

---

Summary

| Service | Technology | Collections / Keys |
|----------|------------|-------------------|
| Message | MongoDB | 5 Collections |
| Search | MongoDB | 3 Collections |
| Realtime | Redis | 3 Key Patterns |

---

End of Part 2

Next

Notification

Media

Admin

Payment

Indexes

Constraints

Performance Design


# 04_Database_Design.md

## PART 3 — Remaining PostgreSQL Services

> Version: 1.0
> Status: FINAL

---

# 10. Notification Service Database

Owner

```
Notification Service
```

Database

```
notification_db
```

Technology

```
PostgreSQL
```

Tables

```
notifications
```

---

# notifications

Purpose

Stores notification history for users.

Notification preferences remain in User Service.

---

Columns

| Column | Type | Notes |
|----------|------|------|
| id | UUID | PK |
| recipient_id | UUID | User receiving notification |
| template_key | VARCHAR(100) | Notification template |
| payload | JSONB | Template variables |
| status | ENUM | Delivery state |
| read_at | TIMESTAMPTZ | Nullable |
| created_at | TIMESTAMPTZ | Required |

---

Status

```
UNREAD

READ

DELETED
```

---

Example

```json
{
  "template_key":"MESSAGE_RECEIVED",

  "payload":{
      "sender":"John",
      "chatName":"Backend Team"
  }
}
```

---

Indexes

```
INDEX(recipient_id)

INDEX(status)

INDEX(created_at DESC)
```

---

Business Rules

- Notifications are immutable.
- Reading only updates read_at.
- Soft delete only.

---

Lifecycle

Created

↓

Read

↓

Deleted

---

Design Rationale

Using template keys keeps notifications language-independent and avoids storing duplicated text.

---

# 11. Media Service Database

Owner

```
Media Service
```

Database

```
media_db
```

Technology

```
PostgreSQL
```

Tables

```
media
```

---

# media

Purpose

Stores metadata for uploaded files.

Actual binary files reside in S3-compatible storage.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| owner_id | UUID |
| original_name | VARCHAR(255) |
| storage_key | TEXT |
| mime_type | VARCHAR(100) |
| file_size | BIGINT |
| checksum | VARCHAR(128) |
| status | ENUM |
| created_at | TIMESTAMPTZ |
| updated_at | TIMESTAMPTZ |

---

Status

```
PENDING

ACTIVE

UNREFERENCED

DELETED
```

---

State Machine

```
Upload Requested

↓

PENDING

↓

Upload Completed

↓

ACTIVE

↓

Message Deleted

↓

UNREFERENCED

↓

Cleanup Job

↓

DELETED
```

---

Indexes

```
INDEX(owner_id)

INDEX(status)

INDEX(mime_type)

INDEX(created_at)
```

---

Business Rules

- storage_key immutable.
- checksum immutable.
- ACTIVE media downloadable.
- UNREFERENCED media retained until cleanup.

---

Design Rationale

Separating metadata from object storage allows storage providers to change without affecting other services.

---

# 12. Admin Service Database

Owner

```
Admin Service
```

Database

```
admin_db
```

Tables

```
admins

audit_logs
```

---

# admins

Purpose

Stores administrator accounts.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| email | VARCHAR(255) |
| role | ENUM |
| created_at | TIMESTAMPTZ |

---

Role

```
SUPER_ADMIN

ADMIN

MODERATOR
```

---

Indexes

```
UNIQUE(email)

INDEX(role)
```

---

# audit_logs

Purpose

Stores every administrative action.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| admin_id | UUID |
| action | VARCHAR(150) |
| target_type | VARCHAR(50) |
| target_id | UUID |
| details | JSONB |
| created_at | TIMESTAMPTZ |

---

Indexes

```
INDEX(admin_id)

INDEX(target_type)

INDEX(created_at DESC)
```

---

Business Rules

Audit logs are append-only.

Never updated.

Never deleted.

---

Example

```json
{
  "action":"USER_SUSPENDED",

  "details":{
      "reason":"Spam"
  }
}
```

---

# 13. Payment Service Database

Owner

```
Payment Service
```

Database

```
payment_db
```

Tables

```
subscriptions

payments
```

---

# subscriptions

Purpose

Tracks active and historical subscriptions.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| user_id | UUID |
| plan | ENUM |
| status | ENUM |
| started_at | TIMESTAMPTZ |
| expires_at | TIMESTAMPTZ |
| cancelled_at | TIMESTAMPTZ |

---

Status

```
ACTIVE

EXPIRED

CANCELLED
```

---

Indexes

```
INDEX(user_id)

INDEX(status)

INDEX(expires_at)
```

---

Business Rules

One ACTIVE subscription per user.

---

# payments

Purpose

Stores every payment transaction.

---

Columns

| Column | Type |
|----------|------|
| id | UUID |
| subscription_id | UUID |
| gateway_transaction_id | VARCHAR(255) |
| amount | DECIMAL(10,2) |
| currency | CHAR(3) |
| status | ENUM |
| provider | VARCHAR(50) |
| paid_at | TIMESTAMPTZ |
| created_at | TIMESTAMPTZ |

---

Status

```
PENDING

SUCCESS

FAILED

REFUNDED
```

---

Indexes

```
UNIQUE(gateway_transaction_id)

INDEX(subscription_id)

INDEX(status)

INDEX(paid_at DESC)
```

---

Business Rules

Webhook confirmation changes payment state.

Frontend callbacks never change payment status.

---

# Cross-Service Summary

| Service | Database | Tables |
|----------|----------|--------|
| Notification | notification_db | 1 |
| Media | media_db | 1 |
| Admin | admin_db | 2 |
| Payment | payment_db | 2 |

---

# PostgreSQL Indexing Strategy

Every service should index:

Primary Key

↓

UUID

Foreign Keys

↓

Always indexed

Frequently filtered columns

↓

Indexed

Frequently sorted columns

↓

Indexed

Large TEXT columns

↓

Avoid indexing unless required

---

Recommended Composite Indexes

```
friend_requests

(sender_id,status)
```

```
chat_members

(user_id,chat_id)
```

```
payments

(user_id,paid_at DESC)
```

```
notifications

(recipient_id,status)
```

---

# Constraints

Every database should enforce business rules whenever practical.

Examples

Unique

```
username
```

```
email
```

```
phone
```

Composite

```
(chat_id,user_id)
```

```
(poll_id,user_id)
```

Check Constraints

```
file_size > 0

amount >= 0
```

---

# Soft Delete Policy

Soft Delete

✔ Chats

✔ Messages

✔ Notifications

✔ Media References

Hard Delete

✔ Temporary Refresh Tokens

✔ Cleanup Jobs

✔ Expired Upload Sessions

---

Summary

Relational Tables

| Service | Tables |
|----------|--------|
| Auth | 2 |
| User | 5 |
| Chat | 4 |
| Notification | 1 |
| Media | 1 |
| Admin | 2 |
| Payment | 2 |

Total

**17 PostgreSQL Tables**

---

End of Part 3

Next

Cross-Service Relationships

Database ER Diagrams

Migration Strategy

Performance Guidelines

Backup Strategy

Transactions

Consistency Model

Scalability

Naming Standards

Database Governance

# 04_Database_Design.md

## PART 4 — Database Governance & Operational Design

> Version: 1.0
> Status: FINAL

---

# 14. Cross-Service Data Ownership

Every database object has exactly one owner.

```
                    USER SERVICE
                  +-------------+
                  |    users    |
                  +-------------+

                         ▲
                         │ REST / Kafka
                         │

+------------+     +-------------+      +--------------+
| Auth DB    |     | Chat DB     |      | Payment DB   |
| credentials|     | chats       |      | subscriptions|
+------------+     +-------------+      +--------------+

                         │
                         │

                 +------------------+
                 | Message MongoDB  |
                 +------------------+

                         │

                 +------------------+
                 | Search MongoDB   |
                 +------------------+

                         │

                 +------------------+
                 | Redis Presence   |
                 +------------------+
```

**Golden Rule**

A service may only modify its own database.

No exceptions.

---

# 15. Logical Entity Relationships

Although databases are isolated, logical relationships exist between entities.

```
User
 │
 ├── Credentials (Auth Service)
 │
 ├── Preferences
 │
 ├── Friendships
 │
 ├── Chat Memberships
 │
 ├── Messages
 │
 ├── Notifications
 │
 ├── Media
 │
 ├── Payments
 │
 └── Subscriptions
```

These relationships are enforced by business logic—not cross-database foreign keys.

---

# 16. Foreign Key Policy

Within a single service database:

✔ Use foreign keys where appropriate.

Across services:

❌ Never create foreign keys.

Example:

Correct

```
chat_members.chat_id

↓

references chats.id
```

Incorrect

```
credentials.user_id

↓

references user_db.users.id
```

Cross-service references are logical only.

---

# 17. Transaction Boundaries

Transactions never span services.

Example:

### Correct

```
User Service

BEGIN

↓

Insert Friend Request

↓

COMMIT

↓

Publish Kafka Event
```

### Incorrect

```
User Service

↓

Chat Database

↓

Notification Database

↓

Payment Database

↓

One Transaction
```

Distributed transactions are prohibited.

---

# 18. Transactional Outbox Pattern

Every service that publishes Kafka events should implement the Transactional Outbox Pattern.

Flow

```
BEGIN

↓

Business Update

↓

Insert Outbox Record

↓

COMMIT

↓

Outbox Publisher

↓

Kafka
```

Benefits

- Eliminates dual-write problems.
- Prevents lost events.
- Supports reliable retries.
- Simplifies recovery after failures.

---

# 19. Database Migration Strategy

All relational schema changes must be version-controlled.

Recommended tool

```
Flyway
```

Migration naming

```
V1__Initial_Schema.sql

V2__Create_Chat_Tables.sql

V3__Add_User_Preferences.sql
```

Rules

- Never edit an applied migration.
- Every schema change creates a new migration.
- Migrations are immutable.

---

# 20. MongoDB Migration Strategy

MongoDB is schema-flexible, but migrations are still required.

Recommended approach

- Application startup migration runner.
- Versioned migration scripts.
- Backward-compatible document changes.
- Gradual migration for large collections.

Example

```
Version 1

↓

No metadata field

↓

Version 2

↓

metadata added

↓

Application populates lazily
```

---

# 21. Redis Data Policy

Redis stores only transient state.

Never store:

- User profiles
- Chats
- Messages
- Payments

Redis may be flushed without data loss.

The application must rebuild presence from active connections.

---

# 22. Consistency Model

The platform follows a hybrid consistency model.

| Operation | Consistency |
|------------|-------------|
| Login | Strong |
| Profile Update | Strong |
| Chat Creation | Strong |
| Message Creation | Strong |
| Notifications | Eventual |
| Search Index | Eventual |
| Presence | Eventual |
| Analytics (future) | Eventual |

---

# 23. Backup Strategy

## PostgreSQL

- Daily full backup
- Hourly WAL archiving (recommended)
- Point-in-time recovery (PITR)

## MongoDB

- Daily snapshots
- Oplog backup (recommended)

## Redis

No backup required for presence data.

Persistence (AOF/RDB) is optional in v1.

---

# 24. Restore Strategy

Recovery priority

1. PostgreSQL
2. MongoDB
3. Kafka
4. Redis

After restore

```
Replay Kafka Events

↓

Rebuild Search Projection

↓

Reconnect Realtime Clients
```

---

# 25. Performance Guidelines

## PostgreSQL

- Index foreign keys.
- Use pagination.
- Avoid `SELECT *`.
- Prefer prepared statements.
- Review slow query logs regularly.

## MongoDB

- Query through indexed fields.
- Page using `sequenceNo`.
- Avoid large embedded arrays.
- Keep documents under MongoDB's size limit.

## Redis

- Use predictable key patterns.
- Apply TTL where appropriate.
- Avoid storing large payloads.

---

# 26. Pagination Strategy

## PostgreSQL

Offset pagination is acceptable for moderate-sized datasets.

```
LIMIT 20 OFFSET 40
```

For large datasets, consider keyset pagination.

---

## MongoDB

Always paginate messages using `sequenceNo`.

```
chatId

+

sequenceNo
```

Avoid skip-based pagination for message history.

---

# 27. Naming Standards

## Databases

```
auth_db

user_db

chat_db
```

## Tables

Plural

```
users

messages

subscriptions
```

## Collections

Plural

```
messages

poll_votes

message_receipts
```

## Redis Keys

```
presence:{userId}

connections:{userId}

socket:{socketId}
```

---

# 28. Auditing Strategy

Audit business events where appropriate.

Examples

- User suspended
- Group archived
- Subscription activated
- Payment completed

Do not audit high-volume transient events such as:

- Typing indicators
- Presence changes
- Heartbeats

---

# 29. Retention Policy

| Data | Policy |
|------|--------|
| Credentials | Retain while account exists |
| User Profiles | Retain while account exists |
| Chats | Soft delete |
| Messages | Soft delete |
| Notifications | Configurable retention |
| Media Metadata | Retain until cleanup |
| Payments | Long-term retention |
| Audit Logs | Long-term retention |

Retention periods may vary according to legal and business requirements.

---

# 30. Capacity Planning (Initial)

| Storage | Primary Growth Driver |
|----------|----------------------|
| PostgreSQL | Users, Chats, Payments |
| MongoDB | Messages |
| Redis | Active Connections |
| S3 Storage | Media Uploads |

Expected largest data store:

```
Message Service (MongoDB)
```

Expected fastest-growing storage:

```
Object Storage (Media)
```

---

# 31. Scalability Strategy

## PostgreSQL

- Read replicas (future)
- Connection pooling
- Partition large tables if needed

## MongoDB

Future sharding strategy

```
Shard Key

↓

chatId
```

## Redis

Horizontal scaling via Redis Cluster when needed.

---

# 32. Security Guidelines

- Encrypt data in transit (TLS).
- Encrypt backups.
- Never store plaintext passwords.
- Hash passwords using Argon2id or BCrypt (Argon2id preferred for new deployments).
- Store secrets outside the database.
- Restrict database access to owning services only.

---

# 33. Database Monitoring

Monitor

## PostgreSQL

- Slow queries
- Lock contention
- Active connections
- Index usage
- Replication lag (future)

## MongoDB

- Query latency
- Index efficiency
- Document growth
- Cache utilization

## Redis

- Memory usage
- Evictions
- Connected clients
- Command latency

---

# 34. Database Governance

Every schema change requires:

1. Design review.
2. Migration script.
3. Index review.
4. Performance review.
5. Documentation update.
6. Automated tests.
7. Rollback strategy.

No undocumented database object may be added.

---

# 35. Final Architecture Summary

## PostgreSQL

| Service | Tables |
|----------|--------|
| Auth | 2 |
| User | 5 |
| Chat | 4 |
| Notification | 1 |
| Media | 1 |
| Admin | 2 |
| Payment | 2 |

**Total: 17 tables**

---

## MongoDB

| Service | Collections |
|----------|-------------|
| Message | 5 |
| Search | 3 |

**Total: 8 collections**

---

## Redis

| Service | Key Patterns |
|----------|--------------|
| Realtime | 3 |

---

## Overall Persistence Landscape

| Technology | Count |
|------------|------:|
| PostgreSQL Databases | 7 |
| PostgreSQL Tables | 17 |
| MongoDB Databases | 2 |
| MongoDB Collections | 8 |
| Redis Databases | 1 |
| Redis Key Patterns | 3 |

---

# Final Notes

The persistence layer is designed around four core principles:

- Clear ownership
- Independent evolution
- High scalability
- Operational simplicity

Each service owns its data, communicates through REST or Kafka, and remains independently deployable.

This design intentionally avoids shared databases, distributed transactions, and cross-service foreign keys, enabling the platform to scale while maintaining strong service boundaries.

---

**Status:** FINAL

**Version:** 1.0

**Next Document:** `05_Folder_Structure.md`