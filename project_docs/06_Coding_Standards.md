# 06_Coding_Standards.md

# PART 1 — General Engineering Standards

Version: 1.0
Status: FINAL

---

# 1. Purpose

This document defines the mandatory engineering standards for the Chat Platform.

Goals

- Consistent code
- High maintainability
- Predictable architecture
- Easy onboarding
- AI-friendly codebase

---

# 2. RFC 2119 Terminology

| Word | Meaning |
|-------|----------|
| MUST | Mandatory |
| MUST NOT | Prohibited |
| SHOULD | Strong recommendation |
| MAY | Optional |

---

# 3. Engineering Principles

Every service MUST follow:

- SOLID
- DRY
- KISS
- YAGNI
- Clean Code
- Clean Architecture
- Package-by-Feature

---

# 4. Clean Code Rules

Code MUST be:

- Readable
- Predictable
- Testable
- Maintainable
- Self-documenting

Avoid clever code.

Prefer obvious code.

---

# 5. Class Responsibilities

Every class MUST have one responsibility.

Good

```
UserRegistrationService
```

Bad

```
UserService
```

(Registers users, sends email, validates password, uploads image...)

---

# 6. Method Rules

Methods SHOULD

- do one thing
- have one responsibility
- be easy to test

Maximum recommended length

30 lines

---

# 7. Method Naming

Methods MUST express intent.

Good

```
registerUser()

archiveChat()

sendMessage()

acceptFriendRequest()
```

Bad

```
doTask()

execute()

run()

handle()

process()
```

---

# 8. Variable Naming

Good

```
registeredUser

chatMember

friendRequest

activeSubscription
```

Bad

```
obj

tmp

list1

value

data
```

---

# 9. Boolean Naming

Good

```
isActive

isDeleted

hasPermission

canEdit
```

Bad

```
activeFlag

deleteStatus
```

---

# 10. Constants

Use

```
UPPER_SNAKE_CASE
```

Example

```
MAX_CHAT_MEMBERS

JWT_EXPIRATION

DEFAULT_LANGUAGE
```

Never use magic numbers.

---

# 11. Package Naming

Lowercase only

Good

```
chat

message

friendship
```

Never

```
Chat

UserModule

MyPackage
```

---

# 12. Class Naming

Use nouns.

Good

```
UserController

ChatRepository

MessageMapper
```

Never

```
DoUser

Manager

Processor

Helper
```

Unless the term has a very specific architectural meaning.

---

# 13. Interface Naming

Avoid

```
IUserService
```

Prefer

```
UserRepository

NotificationSender

PaymentGateway
```

Implementation

```
JpaUserRepository

KafkaNotificationSender

StripePaymentGateway
```

---

# 14. Constructor Injection

Always use constructor injection.

Never use field injection.

Good

```
@Service

public class UserService {

   private final UserRepository repository;

   public UserService(UserRepository repository){

      this.repository = repository;

   }

}
```

Bad

```
@Autowired

private UserRepository repository;
```

---

# 15. Immutability

Prefer immutable objects.

DTOs SHOULD be immutable.

Value Objects MUST be immutable.

---

# 16. Records

Use Java Records for immutable DTOs.

Example

```
public record UserResponse(

UUID id,

String username

){}
```

---

# 17. DTO Rules

Never expose entities.

Controller

↓

DTO

↓

Mapper

↓

Domain

---

Separate

Request DTO

Response DTO

Internal DTO

---

# 18. Entity Rules

Entities

MUST

- represent persistence

MUST NOT

- contain HTTP logic

MUST NOT

- contain REST annotations

MUST NOT

- expose internal IDs unnecessarily

---

# 19. Mapper Rules

Always use dedicated mapper classes.

Never map inside controllers.

Never map inside repositories.

---

# 20. Exception Rules

Throw meaningful exceptions.

Good

```
UserAlreadyExistsException
```

Bad

```
RuntimeException

Exception
```

---

# 21. Null Handling

Avoid returning null.

Prefer

Optional

Empty Collections

Exceptions

---

# 22. Optional Usage

Good

```
Optional<User>
```

Bad

```
Optional<List<User>>
```

Collections should never be Optional.

---

# 23. Collections

Return empty collections.

Never return null collections.

---

# 24. Utility Classes

Allowed

- UUID
- Date
- Hash
- String
- Collection

Forbidden

Business logic.

---

# 25. Business Logic

Business rules belong inside

Application Layer

↓

Domain Layer

Never controllers.

Never repositories.

---

# 26. Dependency Direction

Interfaces

↓

Application

↓

Domain

↓

Infrastructure

Never reverse.

---

# 27. Comments

Comments SHOULD explain

Why

Never

What

Bad

```java
// increment i

i++;
```

Good

```java
// Retry is limited to prevent duplicate payment processing.
```

---

# 28. TODO Policy

Allowed

```
TODO

FIXME

HACK
```

Every TODO MUST reference an issue or task.

Example

```
TODO (#245): Replace temporary cache implementation.
```

---

# 29. Formatting

Indentation

4 spaces

Maximum line length

120 characters

One public class per file.

---

# 30. Engineering Rules Summary

MUST

✔ Constructor Injection

✔ DTOs

✔ Mappers

✔ Meaningful Names

✔ Immutable DTOs

✔ Single Responsibility

✔ Package-by-Feature

✔ Clean Architecture

MUST NOT

✘ Field Injection

✘ Business Logic in Controllers

✘ Shared Mutable State

✘ Null Collections

✘ Utility Business Classes

---

Status

FINAL

Next

Spring Standards

REST

Validation

Security

Kafka

Persistence

Logging

# 06_Coding_Standards.md

# PART 2 — Spring Boot, REST, Persistence & Messaging Standards

Version: 1.0
Status: FINAL

---

# 31. Spring Boot Principles

Every service MUST be:

- Stateless
- Independently deployable
- Independently testable
- Independently configurable

Never depend on another service's implementation.

Only depend on its API contract.

---

# 32. Controller Standards

Controllers MUST

- Handle HTTP only
- Validate input
- Call Application Service
- Return DTOs

Controllers MUST NOT

- Contain business logic
- Access repositories
- Publish Kafka events
- Build SQL queries

Flow

```
HTTP Request

↓

Controller

↓

Application Service

↓

Domain

↓

Infrastructure

↓

Database
```

---

# 33. REST Endpoint Standards

Use resource-based URLs.

Good

```
GET /users/{id}

POST /messages

PUT /users/me

DELETE /chats/{id}
```

Bad

```
POST /createUser

GET /getChats

POST /sendMessage
```

Use HTTP verbs correctly.

| Method | Purpose |
|---------|----------|
| GET | Read |
| POST | Create |
| PUT | Replace |
| PATCH | Partial Update |
| DELETE | Delete |

---

# 34. Request Validation

All incoming requests MUST be validated.

Example

```java
public record RegisterUserRequest(

@NotBlank String username,

@Email String email,

@NotBlank String password

){}
```

Validation belongs at the API boundary.

---

# 35. Response Standards

Every endpoint returns DTOs.

Never return:

- JPA Entity
- Mongo Document
- Internal Model

Good

```
UserResponse
```

Bad

```
UserEntity
```

---

# 36. Exception Handling

Every service MUST provide a global exception handler.

Use `@RestControllerAdvice`.

Map exceptions to appropriate HTTP status codes.

Example

| Exception | Status |
|-----------|--------|
| UserNotFoundException | 404 |
| ValidationException | 400 |
| UnauthorizedException | 401 |
| ForbiddenException | 403 |
| DuplicateResourceException | 409 |
| InternalException | 500 |

Never expose stack traces to clients.

---

# 37. Standard Error Response

Every error MUST follow one structure.

```json
{
  "timestamp":"2026-01-01T10:00:00Z",
  "status":404,
  "error":"USER_NOT_FOUND",
  "message":"User does not exist.",
  "path":"/users/123",
  "correlationId":"..."
}
```

Never return different error formats.

---

# 38. Logging Standards

Use SLF4J.

Never use:

```
System.out.println()
```

Levels

| Level | Usage |
|--------|-------|
| TRACE | Very detailed diagnostics |
| DEBUG | Development debugging |
| INFO | Business events |
| WARN | Recoverable problems |
| ERROR | Failures |

---

# 39. Logging Rules

Log

- Startup
- Shutdown
- Authentication
- Business events
- External API failures
- Kafka failures

Do NOT log

- Passwords
- JWTs
- Refresh tokens
- Credit card data
- Personal secrets

Always include correlation IDs where available.

---

# 40. Security Standards

Every endpoint MUST be classified as:

- Public
- Authenticated
- Role-based

Authentication

```
JWT Access Token
```

Refresh Token

```
HTTP-only Secure Cookie
```

Passwords

```
Argon2id (preferred)

or

BCrypt
```

Never store plaintext passwords.

---

# 41. Authorization

Authorization belongs in the Application layer.

Never trust IDs supplied by clients without verification.

Example

```
PUT /users/me
```

Use authenticated user identity instead of a client-supplied user ID.

---

# 42. Transaction Management

Use `@Transactional` only for relational database operations.

Transactions MUST remain inside a single service.

Never attempt distributed transactions.

Keep transactions short.

---

# 43. Repository Standards

Repositories

MUST

- Persist data
- Retrieve data

Repositories MUST NOT

- Validate business rules
- Publish events
- Call external APIs

Repositories should be thin.

---

# 44. JPA Standards

Prefer explicit mappings.

Avoid `FetchType.EAGER` unless truly required.

Default recommendation:

```
LAZY
```

Avoid N+1 queries.

Use projections or fetch joins when appropriate.

---

# 45. MongoDB Standards

Keep documents focused.

Avoid excessive embedding.

Recommended maximum document size is well below MongoDB's limit.

Use indexes for all common query patterns.

Never query unindexed large collections in production.

---

# 46. Redis Standards

Redis stores transient data only.

Examples

- Presence
- Sessions (if applicable)
- Rate limits
- Socket mappings

Never use Redis as the system of record.

---

# 47. Kafka Standards

Business services publish domain events.

Consumers MUST be idempotent.

Events are immutable.

Never modify previously published event schemas.

Use versioned event names.

Example

```
message.sent.v1
```

---

# 48. Event Publishing

Publish events only after the database transaction commits.

Recommended pattern

```
Business Update

↓

Transactional Outbox

↓

Kafka Publisher
```

Never perform a direct "database write + Kafka publish" without reliability guarantees.

---

# 49. External API Standards

All external integrations MUST

- Have timeouts
- Support retries where appropriate
- Use circuit breakers (future)
- Log failures
- Return meaningful errors

Never block indefinitely waiting for external systems.

---

# 50. Configuration Standards

Configuration belongs outside code.

Use

```
application.yml

application-dev.yml

application-test.yml

application-prod.yml
```

Secrets MUST come from:

- Environment variables
- Secret manager
- Kubernetes Secrets

Never commit secrets to Git.

---

# 51. Database Migration Standards

Use Flyway for PostgreSQL.

Rules

- One migration per schema change.
- Never edit executed migrations.
- Every migration must be reversible where practical.
- Test migrations before production.

---

# 52. API Versioning

Public APIs MUST be versioned.

Example

```
/api/v1/users
```

Breaking changes require a new version.

Avoid breaking existing clients.

---

# 53. Dependency Injection Rules

Always use constructor injection.

Dependencies should be:

- Required
- Immutable (`final`)

Avoid optional dependencies unless truly necessary.

---

# 54. Time Handling

Store all timestamps in UTC.

Use

```
TIMESTAMPTZ
```

(Java)

```
Instant

OffsetDateTime
```

Avoid `java.util.Date` in new code.

---

# 55. Engineering Checklist

Before merging code:

✔ Input validated

✔ DTOs used

✔ Logging added

✔ Exceptions handled

✔ Tests written

✔ Transactions correct

✔ Security reviewed

✔ No secrets committed

✔ No business logic in controllers

✔ No repository leakage

---

Status

FINAL

Next

Frontend Standards

Testing

Git Workflow

Documentation

Code Reviews

# 06_Coding_Standards.md

# PART 3 — Frontend, Testing & Engineering Workflow

Version: 1.0
Status: FINAL

---

# 56. Frontend Principles

Frontend MUST follow:

- Component-based architecture
- Feature-first organization
- Single Responsibility Principle
- Reusable UI
- Predictable state management

---

# 57. React Component Standards

Components SHOULD

- Have one responsibility
- Be reusable
- Be small

Recommended maximum

```
200 lines
```

Split large components into smaller ones.

---

# 58. Component Naming

Use PascalCase.

Good

```
ChatList.jsx

MessageCard.jsx

NotificationPanel.jsx
```

Bad

```
chatlist.jsx

message.jsx

temp.jsx
```

---

# 59. Hooks

Custom hooks begin with:

```
use
```

Example

```
useAuth()

useChat()

useNotifications()
```

Hooks MUST NOT contain UI rendering.

---

# 60. State Management

Global state only for:

- Authentication
- Current user
- Active chat
- Notifications
- Theme

Prefer local component state whenever possible.

---

# 61. API Calls

Never call fetch/axios directly inside components.

Correct

```
Component

↓

Service Layer

↓

API Client
```

Wrong

```
Component

↓

axios.post(...)
```

---

# 62. Error Handling

Display user-friendly errors.

Never expose:

- Stack traces
- Internal exception names
- Database errors

Log technical details separately.

---

# 63. Frontend Security

Never store:

- Refresh Tokens
- Passwords
- Secrets

Sanitize user-generated content before rendering.

Validate uploaded file types and sizes on the client, but always enforce validation again on the server.

---

# 64. Accessibility

Minimum expectations

- Semantic HTML
- Keyboard navigation
- ARIA labels where required
- Sufficient color contrast
- Visible focus indicators

Accessibility is a feature, not an enhancement.

---

# 65. Testing Pyramid

```
          E2E

     Integration

        Unit
```

Aim for many unit tests, fewer integration tests, and a focused set of end-to-end tests.

---

# 66. Unit Testing

Every business rule SHOULD have unit tests.

Examples

- Validation
- Pricing
- Permissions
- Message rules

Unit tests should be fast and isolated.

---

# 67. Integration Testing

Integration tests verify:

- Database interactions
- REST APIs
- Kafka integration
- Redis integration

Use Testcontainers where appropriate.

---

# 68. End-to-End Testing

Validate complete user journeys.

Examples

- Register
- Login
- Create chat
- Send message
- Upload media
- Purchase subscription

---

# 69. Test Naming

Use descriptive names.

Good

```
shouldCreateChatWhenUsersExist()

shouldRejectDuplicateUsername()
```

Bad

```
test1()

testUser()

runTest()
```

---

# 70. Git Branch Strategy

Recommended

```
main

develop

feature/*

bugfix/*

release/*

hotfix/*
```

---

# 71. Commit Messages

Follow Conventional Commits.

Examples

```
feat(chat): add archive endpoint

fix(auth): refresh token expiry

docs(api): update message contract

refactor(user): simplify mapper
```

---

# 72. Pull Requests

Every PR MUST include

- Description
- Related issue
- Testing evidence
- Screenshots (UI changes)
- Documentation updates (if applicable)

---

# 73. Code Reviews

Review for

- Correctness
- Readability
- Security
- Performance
- Tests
- Architecture compliance

Do not review formatting only.

---

# 74. Documentation Standards

Update documentation when changing

- API contracts
- Database schema
- Kafka events
- Configuration
- Deployment

Documentation and implementation must remain synchronized.

---

# 75. Deprecation Policy

Do not remove public APIs immediately.

Recommended lifecycle

```
Deprecated

↓

Supported

↓

Replacement Available

↓

Removal in Future Version
```

Communicate deprecations clearly.

---

# 76. Engineering Checklist

Before merging

✔ Tests pass

✔ Documentation updated

✔ Lint passes

✔ Security reviewed

✔ API unchanged or versioned

✔ No architectural violations

---

Status

FINAL

Next

Performance

Observability

AI Coding Rules

Definition of Done

Engineering Governance

