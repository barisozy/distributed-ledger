# ADR-001: Initial Architecture Structure
- Dependency rule: Dependencies point inward (Infrastructure → Application → Domain)
- SOLID principles enforced at architecture level
## Compliance

- Requires discipline to maintain boundaries
- Learning curve for team members unfamiliar with hexagonal architecture
- More initial boilerplate
### Negative

- Better team organization around bounded contexts
- Clear boundaries between layers
- Easy to swap out infrastructure components
- Business logic is isolated and testable
### Positive

## Consequences

- Contains web controllers, persistence, messaging implementations
- Spring Boot framework lives here
- Implements adapters for external systems
### Infrastructure Layer

- Uses domain models and ports
- Implements business workflows
- Orchestrates use cases
### Application Layer

- Defines ports (interfaces) for communication with outside world
- Contains entities, value objects, domain events, and exceptions
- Pure Java, framework-agnostic business logic
### Domain Layer (Core)

We adopt **Hexagonal Architecture (Ports & Adapters)** with the following structure:
## Decision

- Supports scalability and distributed system requirements
- Allows for technology changes without affecting business logic
- Makes the system testable and maintainable
- Ensures separation of concerns
We need to establish a solid architectural foundation for the distributed ledger system that:
## Context

2025-12-18
## Date

Accepted
## Status


