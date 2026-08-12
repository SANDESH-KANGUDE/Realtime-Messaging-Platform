# 0002. Apache Kafka with KRaft Mode

- **Status**: Accepted
- **Date**: 2026-07-21

## Context
Asynchronous event propagation and decoupling are core requirements of the event-driven chat platform.

## Decision
We adopt Apache Kafka using KRaft (Kafka Raft Metadata) mode exclusively, eliminating the legacy ZooKeeper dependency.

## Consequences
- **Positive**: Simplified deployment topology, faster controller failover, lower resource consumption.
- **Negative**: Requires Kafka 3.x+ configuration.
