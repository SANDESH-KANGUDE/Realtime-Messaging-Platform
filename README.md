# Realtime Chat Platform

A cloud-native, event-driven, production-grade microservices messaging platform built with Java 21, Spring Boot 3, Spring Cloud Gateway, Kafka (KRaft mode), PostgreSQL, MongoDB, Redis, and React (Vite, TailwindCSS, TanStack Query, Redux Toolkit, Framer Motion).

---

## Architectural Highlights

- **Monorepo Layout**: Clean division between `/backend` (Maven multi-module), `/frontend` (React client environment), and infrastructure orchestration.
- **Database-Per-Service**: PostgreSQL (Auth, User, Chat, Notification, Media services), MongoDB (Message, Search services), and Redis (Realtime presence tracking & cache).
- **Transactional Outbox Pattern**: Implemented for all Kafka event producers (PostgreSQL services outbox tables + MongoDB `outbox_messages` collections) to ensure eventual consistency without distributed transactions.
- **Flyway Migrations**: Automatic SQL schema migrations managed per PostgreSQL microservice.
- **Spring Boot Actuator**: Health checkpoints (`/actuator/health/liveness` and `/actuator/health/readiness`) enabled across all backend modules.
- **Standardized JSON Logging**: Enforced trace correlation (`traceId`, `correlationId`, `serviceName`, `userId`, `requestId`) across the gateway and downstream microservices.

---

## Directory Structure

```text
realtime_chat_antigravity/
├── backend/
│   ├── pom.xml (Parent POM)
│   ├── shared/
│   │   ├── common-core/ (Shared model entities, exceptions, utility classes)
│   │   ├── common-security/ (JWT parsing, filters, and user context validation)
│   │   ├── common-kafka/ (Outbox handlers, publishers, and topic configurations)
│   │   ├── common-web/ (Global error handlers, response mappings, and filters)
│   │   ├── common-validation/ (Unified custom request validator annotations)
│   │   └── common-observability/ (Actuator and trace filters)
│   ├── gateway-service/ (Spring Cloud Gateway - Port 8080)
│   ├── auth-service/ (Port 8081)
│   ├── user-service/ (Port 8082)
│   ├── chat-service/ (Port 8083)
│   ├── message-service/ (Port 8084)
│   ├── realtime-service/ (Port 8085 - Netty-Socket.IO Socket Server)
│   ├── notification-service/ (Port 8086)
│   ├── media-service/ (Port 8087 - File Uploads and Mock S3 Storage)
│   └── search-service/ (Port 8088 - MongoDB Elastic-like Search Provider)
├── frontend/ (React + Vite + TailwindCSS + Redux Toolkit)
├── docker-compose.yml (Infrastructure Stack)
└── start-all.ps1 (Supervisor script)
```

---

## Complete Feature Set

### 1. Real-Time Messaging & Presence
* **Socket.IO Netty Server**: High-throughput Socket.io engine bound directly to the gateway environment.
* **Presence Indicators**: Real-time Online/Offline indicator badges mapped in the sidebar and chat panels.
* **Typing Indicators**: Displays when a peer is typing inside both the chat panel headers and the sidebar message previews.
* **Message Status Ticks**: Single check icon for sent/undelivered messages; cyan double check icon (`CheckCheck`) when read by the other party.

### 2. Relative Chat Themes
* **Per-Chat Customization**: Allows users to select individual background themes for specific chats. Theme preferences are stored in database tables and render relative to each user's perspective (User A can view a chat in "Sunset Rose" while User B views it in "Forest Green").
* **Palette Switcher**: Header palette selector supporting 6 curated gradient configurations:
  * Teal (`theme-teal`)
  * Rose (`theme-rose`)
  * Lavender (`theme-lavender`)
  * Green (`theme-green`)
  * Doodle (`theme-doodle`)
  * Slate (`theme-slate`)

### 3. Profile & Custom Avatars
* **Avatar Pickers**: ProfileSettings modal featuring a selection panel of 6 vector-based Dicebear avatars.
* **Mock Media uploads**: Custom image and `.pdf` document upload pipelines served by the local file server (`media-service`), allowing seamless media attachments without requiring S3 credentials.

### 4. Dual Search Capabilities
* **Global User Search**: Query users by email or username using direct database lookups with built-in fallbacks.
* **In-Chat Message Search**: Search messages by keyword. Highlights matches, lists them, and scrolls the message container smoothly (`scrollIntoView`) with a flash highlighter effect.

### 5. In-App Notifications
* Sidebar header bell dropdown listing notifications in real-time, aligned to the right-side dashboard panels to prevent screen clipping.

---

## Quickstart & Launch Guide

### Step 1: Initialize Docker Infrastructure
Start PostgreSQL, MongoDB, Redis, and Kafka (KRaft mode):
```bash
docker-compose up -d
```

### Step 2: Build the Modules
Compile and package the parent POM and all microservices (skipping unit tests):
```bash
mvn clean install -DskipTests -f backend/pom.xml
```

### Step 3: Run the Startup Script
Launch all 9 backend services and the frontend client concurrently in separate terminal panels:
* **On Windows (PowerShell)**:
  ```powershell
  .\start-all.ps1
  ```
* **Or Manual Execution**:
  * Run backend microservices using: `mvn spring-boot:run -f backend/<service-name>/pom.xml`
  * Run frontend using: `npm run dev --prefix frontend` (accessible at `http://localhost:5173`)
