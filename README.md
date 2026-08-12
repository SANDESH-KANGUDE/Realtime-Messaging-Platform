# Realtime Chat Platform

A cloud-native, event-driven, production-grade microservices messaging platform built with Java 21, Spring Boot 3, Spring Cloud Gateway, Kafka (KRaft mode), PostgreSQL, MongoDB, Redis, and React (Vite, TanStack Query, Zustand, React Hook Form, Zod, Framer Motion).

---

## Architectural Highlights

- **Monorepo Layout**: `/backend` (Maven multi-module), `/frontend` (React JS), `/infrastructure`, `/docs`.
- **Database-Per-Service**: PostgreSQL (Auth, User, Chat, Notification, Media, Admin, Payment), MongoDB (Message, Search), Redis (Realtime presence).
- **Transactional Outbox Pattern**: Implemented for all Kafka event producers (PostgreSQL services outbox table + MongoDB `outbox_messages` collection).
- **Flyway Migrations**: SQL schema migrations per PostgreSQL service.
- **Spring Boot Actuator**: Probes `/actuator/health/liveness` and `/actuator/health/readiness` enabled across all backend microservices.
- **Standardized JSON Logging**: Enforced trace correlation (`traceId`, `correlationId`, `serviceName`, `userId`, `requestId`).

---

## Directory Structure

```text
realtime_chat_antigravity/
├── backend/
│   ├── pom.xml (Parent POM)
│   ├── shared/
│   │   ├── common-core/
│   │   ├── common-security/
│   │   ├── common-kafka/
│   │   ├── common-web/
│   │   ├── common-validation/
│   │   └── common-observability/
│   ├── gateway-service/ (Spring Cloud Gateway - Port 8080)
│   ├── auth-service/ (Port 8081)
│   ├── user-service/ (Port 8082)
│   ├── chat-service/ (Port 8083)
│   ├── message-service/ (Port 8084)
│   ├── realtime-service/ (Port 8085 - Socket.IO)
│   ├── notification-service/ (Port 8086)
│   ├── media-service/ (Port 8087)
│   ├── search-service/ (Port 8088)
│   ├── admin-service/ (Port 8089)
│   └── payment-service/ (Port 8090)
├── frontend/ (React + Vite + JavaScript)
├── docs/ (ADRs & Handbook)
└── docker-compose.yml
```

---

## Quickstart

### 1. Infrastructure Services
Start PostgreSQL, MongoDB, Redis, and Kafka (KRaft mode):
```bash
docker-compose up -d
```

### 2. Backend Services
Build parent and all modules:
```bash
cd backend
mvn clean verify
```

### 3. Frontend Application
```bash
cd frontend
npm install
npm run dev
```
