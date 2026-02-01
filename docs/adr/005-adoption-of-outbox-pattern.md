# ADR-005: Adoption of Transactional Outbox Pattern for Reliability

Date: 2025-12-19

## Status
Accepted

## Context
The system requires emitting events to Kafka for audit logging and downstream processing whenever a financial transaction occurs.
Previously, we faced a "Dual-Write Problem" where a database transaction could commit, but the subsequent Kafka publication could fail (or vice versa), leading to data inconsistency.

## Decision
We decided to implement the **Transactional Outbox Pattern**.

1.  We introduced an `outbox_events` table in the same database as the business data.
2.  Events are saved to this table within the same ACID transaction as the account balance updates.
3.  A background process (Scheduler) reads from this table and publishes to Kafka.

## Consequences
* **Positive:** Guarantees "At-Least-Once" delivery of events. Data consistency between DB and Kafka is ensured.
* **Negative:** Adds slight latency to event publishing (due to the polling interval). Increases database storage usage.
* **Mitigation:** Implemented a cleanup job to remove processed events after X days.