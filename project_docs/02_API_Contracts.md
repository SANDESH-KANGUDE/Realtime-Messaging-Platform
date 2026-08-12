# 02_API_Contracts.md

> Version: 1.0
> Status: FINAL
> Last Updated: July 2026
> Audience: Backend Engineers, Frontend Engineers, QA, DevOps, AI Coding Agents

---

# Purpose

This document defines every REST API exposed by the Chat Platform.

It standardizes:

- URL conventions
- Request DTOs
- Response DTOs
- Authentication
- Authorization
- Validation
- Pagination
- Error Handling
- Internal APIs
- Idempotency

This document is the single source of truth for all REST APIs.

---

# 1. Global API Standards

---

## Base URL

```
https://api.chatplatform.com
```

Development

```
http://localhost:8080
```

---

## API Versioning

Every endpoint must follow

```
/api/v1/
```

Example

```
GET /api/v1/users/me
```

Future versions

```
/api/v2/
```

must coexist without breaking clients.

---

## HTTP Methods

| Method | Usage |
|----------|------------------------|
| GET | Read |
| POST | Create |
| PUT | Update |
| PATCH | Partial Update |
| DELETE | Delete |

---

## Content Type

Requests

```
Content-Type: application/json
```

Responses

```
application/json
```

Media Upload

```
multipart/form-data
```

---

# Authentication

Every protected endpoint requires

```
Authorization

Bearer <JWT>
```

Public endpoints

- Login
- Register
- Refresh Token

Everything else requires authentication.

---

# Standard Success Response

```json
{
  "success": true,
  "data": {}
}
```

---

# Standard Error Response

```json
{
  "timestamp": "...",
  "status": 404,
  "code": "USER_001",
  "message": "User not found",
  "path": "/api/v1/users/123"
}
```

---

# Pagination

Endpoints returning collections support

```
?page=0

&size=20

&sort=createdAt,desc
```

Response

```json
{
  "page":0,
  "size":20,
  "totalElements":150,
  "totalPages":8,
  "content":[]
}
```

---

# Idempotency

The following APIs should support idempotency where applicable:

- Payment Creation
- Media Upload Finalization
- Friend Request Submission

Clients send:

```
Idempotency-Key
```

---

# Rate Limiting

Implemented by Spring Cloud Gateway.

Examples

| Endpoint | Limit |
|------------|---------------|
| Login | 5/min |
| Register | 5/min |
| Search | 30/min |
| Upload | 20/min |
| Messages | 120/min |

---

# Internal APIs

Internal APIs

```
/internal/*
```

Rules

- Never exposed publicly.
- Accessible only through service-to-service authentication.
- Not documented in public OpenAPI.

---

# 2. Auth Service APIs

---

## Base Path

```
/api/v1/auth
```

---

### Register

```
POST /register
```

Authentication

```
Public
```

Request

```json
{
  "email":"john@example.com",
  "phone":"+919999999999",
  "password":"******",
  "displayName":"John"
}
```

Response

```json
{
  "userId":"UUID",
  "message":"Registration Successful"
}
```

---

### Login

```
POST /login
```

Request

```json
{
  "email":"john@example.com",
  "password":"******"
}
```

Response

```json
{
  "accessToken":"JWT",
  "refreshToken":"JWT",
  "expiresIn":3600
}
```

---

### Refresh Token

```
POST /refresh
```

Request

```json
{
  "refreshToken":"..."
}
```

Response

```json
{
  "accessToken":"..."
}
```

---

### Logout

```
POST /logout
```

Authentication

```
Required
```

---

### Validate Token

```
GET /validate
```

Internal API

Used by Gateway.

---

# Auth Error Codes

| Code | Meaning |
|----------|----------------|
| AUTH_001 | Invalid Credentials |
| AUTH_002 | Token Expired |
| AUTH_003 | Invalid Token |
| AUTH_004 | Refresh Token Expired |

---

# 3. User Service APIs

---

## Base Path

```
/api/v1/users
```

---

### Get Current User

```
GET /me
```

Returns logged-in profile.

---

### Update Profile

```
PUT /me
```

Request

```json
{
  "displayName":"John",
  "bio":"Software Engineer"
}
```

---

### Get User

```
GET /{userId}
```

---

### Search Users

```
GET /search
```

Query

```
?q=john
```

(Frontend should generally use the Search Service for global search. This endpoint is intended for service-specific lookups.)

---

## Friend APIs

---

### Send Friend Request

```
POST /friends/request
```

Request

```json
{
  "receiverId":"UUID"
}
```

---

### Accept Friend Request

```
PUT /friends/request/{requestId}/accept
```

---

### Reject Friend Request

```
PUT /friends/request/{requestId}/reject
```

---

### Remove Friend

```
DELETE /friends/{friendId}
```

---

### Get Friend List

```
GET /friends
```

---

### Block User

```
POST /block/{userId}
```

---

### Unblock User

```
DELETE /block/{userId}
```

---

### Get Blocked Users

```
GET /block
```

---

## Preferences

---

### Get Preferences

```
GET /preferences
```

---

### Update Preferences

```
PUT /preferences
```

Example

```json
{
  "notifications":true,
  "darkMode":true
}
```

---

## Internal APIs

```
GET /internal/users/{id}
```

Returns

- Display Name
- Username
- Profile Picture

---

```
GET /internal/users/{id}/exists
```

Returns

```
true/false
```

---

```
GET /internal/users/{id}/preferences
```

Used by Notification Service.

---

```
GET /internal/users/{id}/blocked
```

Used by Chat Service.

---

## User Error Codes

| Code | Meaning |
|----------|----------------|
| USER_001 | User Not Found |
| USER_002 | Already Friends |
| USER_003 | Friend Request Exists |
| USER_004 | User Blocked |
| USER_005 | Invalid Preference |

---

# Authorization Matrix (Current Services)

| Endpoint | Auth Required | Owner/Admin |
|------------|----------------|----------------|
| GET /users/me | Yes | Owner |
| PUT /users/me | Yes | Owner |
| GET /users/{id} | Yes | Any Authenticated User (subject to privacy rules) |
| POST /friends/request | Yes | Sender |
| PUT /friends/request/{id}/accept | Yes | Receiver |
| DELETE /friends/{id} | Yes | Owner |
| POST /block/{id} | Yes | Owner |
| PUT /preferences | Yes | Owner |
| POST /auth/register | No | Public |
| POST /auth/login | No | Public |
| POST /auth/refresh | No | Public |
| POST /auth/logout | Yes | Owner |

---

# Validation Rules

## Auth

- Email must be unique.
- Phone number must be unique.
- Password follows configured complexity policy.
- Refresh tokens must be valid and not revoked.

## User

- Display name length configurable (e.g., 3–100 characters).
- Bio length configurable.
- Cannot send a friend request to yourself.
- Duplicate friend requests are rejected.
- Blocked users cannot exchange friend requests.
- Preferences must conform to supported options.

---

**End of Part 1**

**Next Section:**
- Chat Service APIs
- Message Service APIs
- Realtime Service APIs


# 02_API_Contracts.md

## PART 2

---

# 4. Chat Service APIs

## Base Path

```
/api/v1/chats
```

The Chat Service owns:

- Direct Chats
- Group Chats
- Membership
- Group Metadata
- User Chat Settings

It DOES NOT own messages.

---

# Direct Chat APIs

---

## Create or Get Direct Chat

```
POST /direct
```

Authentication

```
Required
```

Request

```json
{
  "receiverId":"UUID"
}
```

Response

```json
{
  "chatId":"UUID",
  "type":"DIRECT"
}
```

Behavior

- If a direct chat already exists, return it.
- Otherwise create one.
- Blocked users cannot create chats.

---

## Get Direct Chats

```
GET /
```

Returns

All chats of current user.

Supports

```
?page

&size

&sort
```

---

## Get Chat Details

```
GET /{chatId}
```

Returns

- Chat Information
- Members
- Settings

---

# Group APIs

---

## Create Group

```
POST /groups
```

Request

```json
{
  "name":"Spring Boot",
  "description":"Backend Discussion",
  "members":[
      "UUID",
      "UUID"
  ]
}
```

Response

```json
{
   "chatId":"UUID"
}
```

---

## Update Group

```
PUT /groups/{chatId}
```

Owner/Admin only.

---

## Add Members

```
POST /groups/{chatId}/members
```

Request

```json
{
   "memberIds":[
      "UUID",
      "UUID"
   ]
}
```

---

## Remove Member

```
DELETE /groups/{chatId}/members/{memberId}
```

---

## Leave Group

```
POST /groups/{chatId}/leave
```

---

## Promote Member

```
PUT /groups/{chatId}/admins/{memberId}
```

---

## Demote Admin

```
DELETE /groups/{chatId}/admins/{memberId}
```

---

## Archive Group

```
PUT /groups/{chatId}/archive
```

Soft archive.

No deletion.

---

# Chat Settings APIs

Per-user settings.

---

## Pin Chat

```
PUT /{chatId}/pin
```

---

## Unpin Chat

```
DELETE /{chatId}/pin
```

---

## Archive Chat

```
PUT /{chatId}/archive
```

---

## Unarchive Chat

```
DELETE /{chatId}/archive
```

---

## Mute Chat

```
PUT /{chatId}/mute
```

Request

```json
{
   "duration":"8H"
}
```

---

## Unmute Chat

```
DELETE /{chatId}/mute
```

---

# Internal APIs

---

## Verify Membership

```
GET /internal/chats/{chatId}/members/{userId}
```

Returns

```
true / false
```

---

## Get Members

```
GET /internal/chats/{chatId}/members
```

Used by:

- Notification
- Realtime

---

## Chat Error Codes

| Code | Meaning |
|------|----------|
| CHAT_001 | Chat Not Found |
| CHAT_002 | Already Exists |
| CHAT_003 | Member Exists |
| CHAT_004 | Member Not Found |
| CHAT_005 | Permission Denied |
| CHAT_006 | Group Archived |

---

# 5. Message Service APIs

Base Path

```
/api/v1/messages
```

Owns

- Messages
- Replies
- Polls
- Reactions
- Delivery
- Read Receipts

MongoDB

---

## Send Message

```
POST /
```

Request

```json
{
   "chatId":"UUID",
   "type":"TEXT",
   "content":"Hello"
}
```

Media

```json
{
   "chatId":"UUID",
   "type":"IMAGE",
   "mediaId":"UUID"
}
```

Response

```json
{
   "messageId":"UUID",
   "sequenceNo":145
}
```

---

## Get Messages

```
GET /chat/{chatId}
```

Supports

```
page

size

beforeSequence

afterSequence
```

---

## Get Message

```
GET /{messageId}
```

---

## Edit Message

```
PUT /{messageId}
```

Allowed

Within

```
3 Minutes
```

Request

```json
{
    "content":"Updated Text"
}
```

---

## Delete Message

```
DELETE /{messageId}
```

Soft Delete.

Message becomes

```
This message was deleted
```

---

## Reply

```
POST /reply
```

Request

```json
{
   "chatId":"UUID",
   "replyTo":"UUID",
   "content":"Nice!"
}
```

---

# Reaction APIs

---

## Add Reaction

```
POST /{messageId}/reactions
```

```json
{
   "emoji":"❤️"
}
```

---

## Remove Reaction

```
DELETE /{messageId}/reactions
```

---

# Read Receipts

---

## Mark Read

```
PUT /{messageId}/read
```

---

## Mark Multiple Read

```
PUT /read
```

```json
{
   "messageIds":[
      "...",
      "..."
   ]
}
```

---

# Poll APIs

---

## Create Poll

```
POST /polls
```

---

## Vote

```
POST /polls/{pollId}/vote
```

---

## Close Poll

```
PUT /polls/{pollId}/close
```

---

# Internal APIs

---

## Validate Message

```
GET /internal/messages/{id}
```

---

## Message Error Codes

| Code | Meaning |
|------|----------|
| MSG_001 | Message Not Found |
| MSG_002 | Edit Window Expired |
| MSG_003 | Already Deleted |
| MSG_004 | Invalid Reply |
| MSG_005 | Media Missing |
| MSG_006 | Poll Closed |

---

# 6. Realtime Service APIs

Unlike other services,

Realtime primarily exposes

Socket.IO events.

REST APIs are minimal.

---

## Base Path

```
/socket.io
```

Authentication

JWT Handshake

---

# Client → Server Events

---

## Join Chat

```
join-chat
```

Payload

```json
{
   "chatId":"UUID"
}
```

---

## Leave Chat

```
leave-chat
```

---

## Typing Start

```
typing-start
```

Payload

```json
{
   "chatId":"UUID"
}
```

---

## Typing Stop

```
typing-stop
```

---

## Heartbeat

```
heartbeat
```

Every

```
30 seconds
```

---

# Server → Client Events

---

## New Message

```
message:new
```

---

## Message Edited

```
message:edited
```

---

## Message Deleted

```
message:deleted
```

---

## Reaction Added

```
reaction:added
```

---

## Reaction Removed

```
reaction:removed
```

---

## Poll Updated

```
poll:updated
```

---

## User Online

```
user:online
```

---

## User Offline

```
user:offline
```

---

## Typing Started

```
typing:start
```

---

## Typing Stopped

```
typing:stop
```

---

# Delivery ACK

Every

```
message:new
```

must receive

```
ACK
```

Only after ACK

↓

Message becomes

```
DELIVERED
```

Offline users receive delivery status after synchronization.

---

# Authorization Matrix

| Endpoint | Authentication | Authorization |
|-----------|---------------|--------------|
| POST /messages | JWT | Chat Member |
| PUT /messages/{id} | JWT | Sender |
| DELETE /messages/{id} | JWT | Sender |
| POST /polls | JWT | Chat Member |
| POST /reactions | JWT | Chat Member |
| Join Socket Room | JWT | Chat Member |
| Typing Events | JWT | Chat Member |
| Heartbeat | JWT | Connected User |

---

# Validation Rules

## Chat

- Only members may access chats.
- Archived groups cannot be modified.
- Group admins control membership.
- Direct chats are unique between two users.

---

## Messages

- sequenceNo generated by server.
- Only sender edits messages.
- 3-minute edit limit.
- Soft delete only.
- mediaId must exist.
- Poll options configurable.
- Read receipts idempotent.
- Reactions unique per user.

---

## Realtime

- JWT required during handshake.
- Room join only after membership verification.
- Heartbeat every 30 seconds.
- ACK required for delivery confirmation.
- Typing events expire automatically.

---

End of Part 2

Next

Notification Service

Media Service

Search Service

Admin Service

Payment Service

Global API Appendices

# 02_API_Contracts.md

## PART 3

---

# 7. Notification Service APIs

## Base Path

```
/api/v1/notifications
```

The Notification Service owns:

- Notification History
- Notification Templates
- Notification Delivery Status
- Notification Deduplication

Notification preferences remain the responsibility of the User Service.

---

## Get Notifications

```
GET /
```

Query Parameters

```
?page=0
&size=20
&status=UNREAD
```

Response

```json
{
  "page": 0,
  "size": 20,
  "totalElements": 82,
  "content": [
    {
      "notificationId": "UUID",
      "title": "New Message",
      "body": "John sent you a message.",
      "status": "UNREAD",
      "createdAt": "2026-07-20T14:10:12Z"
    }
  ]
}
```

---

## Mark Notification Read

```
PUT /{notificationId}/read
```

---

## Mark All Read

```
PUT /read-all
```

---

## Delete Notification

```
DELETE /{notificationId}
```

Soft delete.

---

## Internal APIs

### Create Notification

```
POST /internal/notifications
```

Used internally by Kafka consumers only.

Never exposed publicly.

---

## Error Codes

| Code | Meaning |
|--------|----------|
| NOTIF_001 | Notification Not Found |
| NOTIF_002 | Already Read |
| NOTIF_003 | Invalid Template |

---

# 8. Media Service APIs

## Base Path

```
/api/v1/media
```

The Media Service owns

- Upload Metadata
- File Metadata
- Download URL Generation
- Object Lifecycle

Files reside in S3-compatible object storage.

---

## Step 1 — Request Upload URL

```
POST /upload-url
```

Request

```json
{
  "fileName": "photo.jpg",
  "contentType": "image/jpeg",
  "fileSize": 246810
}
```

Response

```json
{
  "mediaId": "UUID",
  "uploadUrl": "...",
  "expiresIn": 900
}
```

---

## Step 2 — Complete Upload

```
POST /{mediaId}/complete
```

The client calls this after successfully uploading to object storage.

---

## Get Metadata

```
GET /{mediaId}
```

---

## Get Download URL

```
GET /{mediaId}/download
```

Response

```json
{
  "downloadUrl": "...",
  "expiresIn": 300
}
```

---

## Internal APIs

### Verify Media

```
GET /internal/media/{mediaId}
```

Returns

```json
{
  "exists": true,
  "status": "ACTIVE"
}
```

Used by Message Service.

---

## Error Codes

| Code | Meaning |
|--------|----------|
| MEDIA_001 | Media Not Found |
| MEDIA_002 | Upload Expired |
| MEDIA_003 | Invalid MIME Type |
| MEDIA_004 | File Too Large |

---

# 9. Search Service APIs

## Base Path

```
/api/v1/search
```

Owns

- User Index
- Chat Index
- Message Index

---

## Search

```
GET /
```

Query Parameters

```
?q=spring
&type=messages
&page=0
&size=20
```

Allowed values

```
users
chats
messages
all
```

Response

```json
{
  "results": []
}
```

Highlights are included when supported.

---

## Error Codes

| Code | Meaning |
|--------|----------|
| SEARCH_001 | Invalid Search Type |
| SEARCH_002 | Empty Query |

---

# 10. Admin Service APIs

## Base Path

```
/api/v1/admin
```

Authentication

```
JWT
```

Authorization

```
ADMIN ROLE REQUIRED
```

---

## Dashboard Summary

```
GET /dashboard
```

Returns

- Total Users
- Active Users
- Total Chats
- Total Messages
- Platform Health (basic v1)

---

## Get Users

```
GET /users
```

Supports pagination.

---

## Suspend User

```
PUT /users/{userId}/suspend
```

---

## Activate User

```
PUT /users/{userId}/activate
```

---

## Get Groups

```
GET /groups
```

---

## Archive Group

```
PUT /groups/{groupId}/archive
```

---

## Publish Announcement

```
POST /announcements
```

Request

```json
{
  "title": "Maintenance",
  "message": "Platform maintenance tonight."
}
```

Produces

```
admin.announcement.v1
```

---

## Audit Logs

```
GET /audit-logs
```

Supports

```
page

size

dateFrom

dateTo
```

---

## Error Codes

| Code | Meaning |
|--------|----------|
| ADMIN_001 | Unauthorized |
| ADMIN_002 | User Not Found |
| ADMIN_003 | Announcement Failed |

---

# 11. Payment Service APIs

## Base Path

```
/api/v1/payments
```

Owns

- Payments
- Subscriptions
- Payment History

---

## Create Payment

```
POST /
```

Headers

```
Idempotency-Key
```

Request

```json
{
  "planId": "PREMIUM_MONTHLY"
}
```

Response

```json
{
  "paymentId": "UUID",
  "paymentUrl": "https://..."
}
```

---

## Payment History

```
GET /history
```

---

## Current Subscription

```
GET /subscription
```

---

## Cancel Subscription

```
DELETE /subscription
```

---

## Webhook Endpoint

```
POST /webhooks/payment-provider
```

Public endpoint.

Verified using the payment gateway signature.

This endpoint activates subscriptions.

Frontend callbacks NEVER activate subscriptions.

---

## Error Codes

| Code | Meaning |
|--------|----------|
| PAY_001 | Payment Failed |
| PAY_002 | Duplicate Transaction |
| PAY_003 | Subscription Already Active |
| PAY_004 | Invalid Webhook Signature |

---

# 12. Common HTTP Status Codes

| Code | Meaning |
|------|----------|
| 200 | Success |
| 201 | Resource Created |
| 202 | Accepted |
| 204 | No Content |
| 400 | Validation Error |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict |
| 422 | Business Rule Violation |
| 429 | Rate Limited |
| 500 | Internal Error |
| 503 | Service Unavailable |

---

# 13. API Naming Conventions

Resources use plural nouns.

Examples

```
/users
/chats
/messages
/payments
/groups
```

Avoid verbs in resource names.

Preferred

```
POST /friends/request
```

Better (RESTful)

```
POST /friend-requests
```

The implementation should favor resource-oriented URLs where practical while maintaining backward compatibility if APIs evolve.

---

# 14. Request Validation Standard

Every write request must validate:

- JWT authentication
- Authorization
- Required fields
- Field length
- Allowed values
- Resource ownership
- Resource existence
- Business rules
- Duplicate operations
- Referential integrity

Validation failures return

```
400
```

Business rule violations return

```
422
```

---

# 15. Response DTO Guidelines

Entities are NEVER returned directly.

Each endpoint exposes dedicated DTOs.

Example

```
UserEntity

↓

UserResponse
```

Never expose

- Passwords
- Refresh Tokens
- Internal IDs
- Storage Keys
- Database-specific metadata

---

# 16. API Evolution Policy

Breaking changes require

```
/api/v2/
```

Non-breaking additions may include

- New optional fields
- New endpoints
- Additional query parameters

Existing clients must continue working.

---

# 17. OpenAPI Standards

Each service publishes

```
/v3/api-docs
```

Swagger UI

```
/swagger-ui.html
```

OpenAPI documentation must include

- Endpoint descriptions
- Request DTOs
- Response DTOs
- Error responses
- Authentication requirements

---

# 18. Service Summary

| Service | Public APIs | Internal APIs |
|----------|------------|---------------|
| Auth | ✓ | ✓ |
| User | ✓ | ✓ |
| Chat | ✓ | ✓ |
| Message | ✓ | ✓ |
| Realtime | Socket.IO | Minimal |
| Notification | ✓ | ✓ |
| Media | ✓ | ✓ |
| Search | ✓ | No |
| Admin | ✓ | No |
| Payment | ✓ | Webhooks |

---

# Final Notes

Every REST endpoint in the platform must adhere to this specification.

Future APIs should follow the same conventions for:

- Versioning
- Authentication
- Authorization
- Validation
- Error Handling
- DTO Design
- Pagination
- Rate Limiting
- Idempotency

This document serves as the definitive REST API contract for the Chat Platform.

---

**Status:** FINAL

**Version:** 1.0

**Next Document:** `03_Event_Contracts.md`