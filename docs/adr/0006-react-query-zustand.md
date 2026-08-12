# 0006. Frontend State Management: TanStack Query + Zustand

- **Status**: Accepted
- **Date**: 2026-07-21

## Context
The React frontend requires clear separation between server state (REST endpoints, chat history, user profiles) and client state (active UI tabs, dark/light theme, active chat selection, socket connection state).

## Decision
We mandate **TanStack Query (`@tanstack/react-query`)** for server data fetching, caching, and invalidation, combined with **Zustand** for lightweight client-side state.

## Consequences
- **Positive**: Eliminates manual cache synchronization, automated retries and refetching, clean component code.
- **Negative**: Requires strict query key naming conventions (`["chats"]`, `["messages", chatId]`).
