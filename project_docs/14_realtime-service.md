# 14_realtime-service.md

Version: 1.0
Status: FINAL

---

# Purpose

Provide realtime communication using Socket.IO.

---

# Responsibilities

- WebSocket Connections
- Presence
- Typing Indicators
- Event Broadcasting
- Connection Management

---

# Redis

Keys

- presence
- connections
- socket

---

# Events

Client

- connect
- disconnect
- typing.start
- typing.stop

Server

- message.created
- user.online
- user.offline
- notification.created

---

# Business Rules

- One authenticated socket session
- Presence timeout
- Auto reconnect

---

# Security

- JWT handshake
- Room authorization
- Rate limiting

---

# Testing

- Connection lifecycle
- Presence
- Typing
- Broadcasting

---

Status: FINAL