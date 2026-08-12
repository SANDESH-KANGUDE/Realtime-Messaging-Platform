# 0005. Socket.IO and Redis Key Schema for Presence

- **Status**: Accepted
- **Date**: 2026-07-21

## Context
Real-time bi-directional messaging, typing indicators, and user presence must scale across multiple socket instances without keeping state in application memory.

## Decision
Realtime Service uses Socket.IO Java Server backed by Redis storing state with strict key naming:
- `presence:{userId}`
- `connections:{userId}`
- `socket:{socketId}`

## Consequences
- **Positive**: Horizontal scalability of WebSocket nodes, sub-millisecond presence lookup.
- **Negative**: Requires Redis heartbeat handling for disconnect cleanup.
