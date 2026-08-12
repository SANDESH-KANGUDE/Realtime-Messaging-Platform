# 05_Folder_Structure.md

# PART 1 — Repository & Backend Project Structure

> Version: 1.0
> Status: FINAL
> Audience: Backend Engineers, Frontend Engineers, DevOps, AI Coding Agents

---

# 1. Purpose

This document defines the official repository structure for the Chat Platform.

It specifies:

- Repository layout
- Maven module organization
- Backend service structure
- Package organization
- Shared libraries
- Naming conventions
- Dependency rules

The goal is to ensure every service follows a consistent structure that scales as the platform grows.

---

# 2. Repository Strategy

The project uses a **Monorepo**.

```
chat-platform/

├── backend/
├── frontend/
├── infrastructure/
├── docs/
├── scripts/
├── .github/
├── docker-compose.yml
├── README.md
└── LICENSE
```

---

Why Monorepo?

Advantages

- Single source of truth
- Easier dependency management
- Unified CI/CD
- Atomic commits across services
- Easier local development
- Better AI code generation context

---

# 3. Root Folder Structure

```
chat-platform/

backend/
frontend/
infrastructure/
docs/
scripts/
.github/

README.md
docker-compose.yml
pom.xml
```

---

Folder Responsibilities

| Folder | Purpose |
|---------|----------|
| backend | All Spring Boot services |
| frontend | React application |
| infrastructure | Docker, Kubernetes, Helm |
| docs | Architecture handbook |
| scripts | Utility scripts |
| .github | GitHub Actions |

---

# 4. Backend Structure

```
backend/

├── pom.xml

├── shared/

├── auth-service/

├── user-service/

├── chat-service/

├── message-service/

├── realtime-service/

├── notification-service/

├── media-service/

├── search-service/

├── payment-service/

└── admin-service/
```

Each folder is an independent Spring Boot application.

---

# 5. Maven Multi-Module Layout

```
backend/

pom.xml

shared/

auth-service/

user-service/

chat-service/

message-service/

...
```

Parent POM

↓

Shared dependency management

↓

Independent executable services

---

Advantages

- Centralized dependency versions
- Faster builds
- Shared plugins
- Independent deployment

---

# 6. Shared Libraries

Purpose

Contains reusable infrastructure only.

Never business logic.

```
shared/

├── common-core/

├── common-security/

├── common-kafka/

├── common-web/

├── common-observability/

├── common-validation/

└── common-test/
```

---

# common-core

Contains

- Base exceptions
- Error responses
- Utilities
- Constants
- UUID helpers
- Time helpers

---

# common-security

Contains

- JWT utilities
- Authentication filters
- Security configuration
- Permission helpers

---

# common-kafka

Contains

- Event envelope
- Kafka serializers
- Kafka configuration
- Topic constants

---

# common-web

Contains

- API response wrapper
- Global exception handler
- Request logging
- Correlation ID filter

---

# common-validation

Contains

- Custom validators
- Validation annotations
- Validation utilities

---

# common-observability

Contains

- Logging configuration
- Metrics
- Tracing helpers

---

# common-test

Contains

- Test containers
- Mock factories
- Integration helpers
- Base test classes

---

Dependency Rule

Shared modules

↓

May not depend on services.

Services

↓

May depend on shared modules.

Never the opposite.

---

# 7. Backend Service Structure

Every service follows exactly the same structure.

Example

```
user-service/

src/

main/

java/

resources/

test/

pom.xml
```

---

# 8. Java Package Structure

```
src/main/java

com/

company/

chatplatform/

userservice/
```

Inside

```
application/

domain/

infrastructure/

interfaces/
```

---

Why?

This separates business rules from frameworks.

---

# 9. Clean Architecture Layers

```
Interfaces

↓

Application

↓

Domain

↓

Infrastructure
```

Dependencies only point inward.

Never outward.

---

# 10. Package-by-Feature

Example

```
userservice/

application/

domain/

infrastructure/

interfaces/
```

Inside

```
user/

friendship/

block/

preferences/
```

Each feature contains its own implementation.

---

Example

```
user/

application/

domain/

infrastructure/

interfaces/
```

---

# 11. Feature Structure

Example

```
user/

application/

services/

commands/

queries/

dto/
```

```
domain/

model/

repository/

events/
```

```
infrastructure/

entity/

repository/

mapper/
```

```
interfaces/

rest/

request/

response/
```

---

Responsibilities

Application

Business use cases.

---

Domain

Pure business rules.

No Spring annotations.

No JPA.

No HTTP.

---

Infrastructure

JPA

Mongo

Kafka

Redis

External APIs

---

Interfaces

REST Controllers

Request DTOs

Response DTOs

---

# 12. Example User Service Structure

```
user-service/

src/main/java

com/company/chatplatform/userservice/

application/

user/

friendship/

block/

preferences/

domain/

user/

friendship/

block/

preferences/

infrastructure/

persistence/

security/

messaging/

configuration/

interfaces/

rest/

user/

friendship/

preferences/

common/
```

---

# 13. Configuration Package

```
configuration/

security/

database/

kafka/

redis/

openapi/

web/

cache/
```

Every configuration class belongs here.

---

# 14. Common Package

```
common/

exception/

mapper/

util/

constants/

validation/
```

Contains service-specific utilities only.

Global utilities belong in shared modules.

---

# 15. Resource Folder

```
resources/

application.yml

application-dev.yml

application-test.yml

application-prod.yml

db/

migration/

messages/

logback.xml
```

---

Responsibilities

| Folder | Purpose |
|----------|----------|
| db/migration | Flyway migrations |
| messages | Localization |
| application-* | Environment configs |
| logback | Logging |

---

# 16. Testing Structure

```
src/test/

java/

resources/
```

Mirror production packages.

Example

```
src/main/java

user/
```

↓

```
src/test/java

user/
```

---

Test Types

```
unit/

integration/

contract/

performance/
```

---

# 17. Dependency Rules

Allowed

Interfaces

↓

Application

↓

Domain

↓

Infrastructure

---

Forbidden

Infrastructure

↓

Interfaces

---

Forbidden

Domain

↓

Spring Framework

---

Forbidden

Application

↓

Controllers

---

# 18. Naming Conventions

Packages

```
lowercase
```

Classes

```
PascalCase
```

Methods

```
camelCase
```

Constants

```
UPPER_SNAKE_CASE
```

Resources

```
kebab-case
```

---

# Summary

Repository

```
Monorepo
```

Backend

```
Maven Multi-Module
```

Architecture

```
Package-by-Feature

+

Clean Architecture
```

Shared Code

```
Infrastructure Only
```

---

End of Part 1

Next

Frontend Structure

Infrastructure Folder

Docker

Kubernetes

GitHub Actions

Scripts

Deployment Structure

Repository Rules


# 05_Folder_Structure.md

# PART 2 — Frontend, Infrastructure & Repository Standards

> Version: 1.0
> Status: FINAL

---

# 19. Frontend Structure

Technology

```
React
```

Project

```
frontend/

├── public/

├── src/

├── package.json

├── vite.config.js

└── README.md
```

---

# 20. Frontend Source Structure

```
src/

assets/

components/

features/

hooks/

layouts/

pages/

router/

services/

store/

styles/

types/

utils/

App.jsx

main.jsx
```

---

Folder Responsibilities

| Folder | Purpose |
|----------|----------|
| assets | Images, icons, fonts |
| components | Reusable UI components |
| features | Business features |
| hooks | Custom React hooks |
| layouts | Page layouts |
| pages | Route-level pages |
| router | React Router configuration |
| services | API clients |
| store | State management |
| styles | Global styles |
| types | Shared TypeScript types (or JS typedefs) |
| utils | Helper utilities |

---

# 21. Feature-Based Frontend Organization

Each feature is self-contained.

Example

```
features/

chat/

conversation/

friends/

notifications/

profile/

settings/

authentication/
```

---

Example

```
chat/

components/

hooks/

pages/

services/

types/

utils/
```

---

Advantages

- Independent development
- Better scalability
- Easier testing
- Clear ownership
- Reusable components

---

# 22. Shared UI Components

```
components/

button/

modal/

avatar/

loader/

input/

dropdown/

toast/

dialog/

table/
```

Only generic UI belongs here.

Business-specific UI remains inside `features/`.

---

# 23. API Client Structure

```
services/

api/

auth/

chat/

message/

notification/

media/

payment/
```

Each folder exposes typed service functions.

Example

```
chat/

createChat()

archiveChat()

getChats()

getMembers()
```

Never call `fetch` or `axios` directly from React components.

---

# 24. State Management

```
store/

auth/

chat/

message/

notification/

ui/
```

Global state only.

Local component state should remain inside components whenever possible.

---

# 25. Infrastructure Folder

```
infrastructure/

docker/

kubernetes/

helm/

monitoring/

database/

gateway/
```

Responsibilities

| Folder | Purpose |
|----------|----------|
| docker | Dockerfiles & Compose |
| kubernetes | Kubernetes manifests |
| helm | Helm charts |
| monitoring | Prometheus, Grafana, alerts |
| database | Initialization scripts |
| gateway | API Gateway configuration |

---

# 26. Docker Structure

```
docker/

auth-service/

user-service/

chat-service/

message-service/

frontend/

docker-compose.dev.yml

docker-compose.prod.yml
```

Each service owns its own Dockerfile.

---

# 27. Kubernetes Structure

```
kubernetes/

base/

auth/

user/

chat/

message/

media/

notification/

payment/

admin/

search/

realtime/

frontend/
```

Each service contains:

```
deployment.yaml

service.yaml

configmap.yaml

secret.yaml

hpa.yaml

ingress.yaml
```

---

# 28. Helm Structure

```
helm/

chat-platform/

templates/

values.yaml

Chart.yaml
```

Purpose

Deploy the complete platform with environment-specific configuration.

---

# 29. Monitoring Structure

```
monitoring/

prometheus/

grafana/

loki/

tempo/

alerts/
```

Responsibilities

| Tool | Purpose |
|------|----------|
| Prometheus | Metrics |
| Grafana | Dashboards |
| Loki | Log aggregation |
| Tempo | Distributed tracing |
| Alerts | Alert rules |

---

# 30. Database Folder

```
database/

postgres/

mongodb/

redis/
```

Contains:

- Initialization scripts
- Local development configuration
- Seed data
- Utility scripts

No production business data is stored here.

---

# 31. Scripts Folder

```
scripts/

build/

deploy/

database/

development/

testing/
```

Example

```
start-local.sh

stop-local.sh

seed-db.sh

run-tests.sh

build-all.sh
```

Scripts should be idempotent where practical.

---

# 32. Documentation Folder

```
docs/

architecture/

api/

deployment/

runbooks/

adr/

```

Responsibilities

| Folder | Purpose |
|----------|----------|
| architecture | Handbook documents |
| api | OpenAPI specs |
| deployment | Deployment guides |
| runbooks | Operational procedures |
| adr | Architecture Decision Records |

---

# 33. GitHub Structure

```
.github/

workflows/

ISSUE_TEMPLATE/

PULL_REQUEST_TEMPLATE.md

CODEOWNERS
```

---

Workflows

```
build.yml

test.yml

lint.yml

security.yml

deploy.yml
```

---

# 34. Repository Standards

Every service must contain:

```
README.md

Dockerfile

pom.xml

src/

```

Every module must build independently.

---

# 35. Environment Configuration

Never commit secrets.

Example

```
application.yml

application-dev.yml

application-test.yml

application-prod.yml
```

Environment variables override configuration values.

---

# 36. Dependency Management

Backend

```
Maven
```

Frontend

```
npm
```

Versions should be centrally managed where possible.

---

# 37. Logging Structure

```
logs/

application.log

error.log
```

In production, logs should be written to stdout/stderr and collected by the platform.

No service should rely on local log files.

---

# 38. Repository Rules

Allowed

- Independent services
- Shared infrastructure libraries
- Shared CI/CD
- Shared documentation

Forbidden

- Shared databases
- Shared business logic
- Circular module dependencies
- Direct service-to-service database access

---

# 39. Folder Naming Standards

Directories

```
kebab-case
```

Java packages

```
lowercase
```

Java classes

```
PascalCase
```

React components

```
PascalCase
```

Configuration files

```
kebab-case
```

Docker images

```
chat-platform/auth-service

chat-platform/user-service
```

---

# 40. Repository Blueprint

```
chat-platform/

├── backend/
│   ├── shared/
│   ├── auth-service/
│   ├── user-service/
│   ├── chat-service/
│   ├── message-service/
│   ├── realtime-service/
│   ├── notification-service/
│   ├── media-service/
│   ├── payment-service/
│   ├── search-service/
│   └── admin-service/
│
├── frontend/
│
├── infrastructure/
│   ├── docker/
│   ├── kubernetes/
│   ├── helm/
│   ├── monitoring/
│   ├── database/
│   └── gateway/
│
├── docs/
│
├── scripts/
│
├── .github/
│
├── docker-compose.yml
├── README.md
└── LICENSE
```

---

# Final Notes

The repository structure is designed around five principles:

- Feature ownership
- Service independence
- Clean Architecture
- Operational simplicity
- Long-term scalability

Every backend service follows the same internal structure.

Every frontend feature is self-contained.

Infrastructure is separated from application code.

This consistency reduces onboarding time, improves maintainability, and enables parallel development across teams.

---

**Status:** FINAL

**Version:** 1.0

**Next Document:** `06_Coding_Standards.md`