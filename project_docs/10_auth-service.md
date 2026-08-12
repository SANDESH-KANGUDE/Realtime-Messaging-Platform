# 10_auth-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Manage authentication, authorization, and user sessions.

---

# Responsibilities

- User Registration
- Login
- Logout
- JWT Generation
- Refresh Token Management
- Password Hashing
- Password Reset
- Email Verification (Future)

---

# Database

Tables

- credentials
- refresh_tokens

---

# APIs

POST /auth/register

POST /auth/login

POST /auth/refresh

POST /auth/logout

POST /auth/forgot-password

POST /auth/reset-password

GET /auth/me

---

# Kafka Events

Publish

- auth.user.registered

Consume

- user.deleted

---

# Security

- BCrypt password hashing
- JWT Access Tokens
- HttpOnly Refresh Tokens
- Rate Limiting
- Account Locking (Future)

---

# Business Rules

- Email must be unique
- Password must meet policy
- Refresh tokens expire
- Invalidated tokens cannot be reused

---

# Testing

- Registration
- Login
- Refresh
- Logout
- Invalid Credentials
- Token Expiry

---

Status: FINAL