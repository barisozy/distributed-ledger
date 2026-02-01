# ADR-003: Adoption of Optimistic Locking Strategy

Date: 2025-12-19
Status: Accepted

## Context
The distributed ledger system requires handling high-volume concurrent transactions on `Account` entities.
In a scenario where multiple requests attempt to modify the same account balance simultaneously (e.g., automated payments, transfers, POS usage), data integrity is paramount.

We evaluated two standard locking strategies:
1.  **Pessimistic Locking:** Locking the database row (`SELECT ... FOR UPDATE`) at the beginning of the transaction.
2.  **Optimistic Locking:** Using a `version` column to detect concurrent modifications at commit time.

## Decision
We decided to adopt **Optimistic Locking** using JPA `@Version` annotation.

## Rationale
1.  **Performance:** Pessimistic locking drastically reduces throughput by serializing access to hot accounts (like a merchant account receiving thousands of payments). Optimistic locking allows non-blocking reads.
2.  **Distributed System Nature:** In a distributed architecture (microservices), holding database locks for the duration of a business transaction (which might involve network calls) is an anti-pattern that leads to connection pool exhaustion and deadlocks.
3.  **Fail-Fast Philosophy:** We prefer failing fast and retrying rather than blocking threads indefinitely.

## Consequences
* **Positive:** Higher system throughput and lower latency for read operations. Zero database deadlocks related to row-level locking.
* **Negative:** Transactions may fail with `OptimisticLockingFailureException` under high contention.
* **Mitigation:** We implemented a `@Retryable` mechanism in the `SendMoneyService` layer. This automatically retries the failed transaction (fetching the latest version and re-applying logic) without user intervention, ensuring a smooth UX.

## Compliance
* All `Entity` classes (specifically `AccountEntity`) MUST include a `@Version` field.
* Service methods modifying these entities MUST be idempotent or safe to retry.