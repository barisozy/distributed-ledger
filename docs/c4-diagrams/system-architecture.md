# C4 Model - System Context Diagram

## Distributed Ledger System

```
┌─────────────────────────────────────────────────────────────────────┐
│                         System Context                               │
└─────────────────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │   End User   │
                    │  (Person)    │
                    └──────┬───────┘
                           │
                           │ Uses
                           ↓
                  ┌────────────────┐
                  │  Distributed   │
                  │ Ledger System  │
                  │  [Software]    │
                  └────────┬───────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ↓                ↓                ↓
   ┌─────────────┐  ┌─────────────┐  ┌──────────────┐
   │ PostgreSQL  │  │    Redis    │  │    Kafka     │
   │ [Database]  │  │   [Cache]   │  │ [Messaging]  │
   └─────────────┘  └─────────────┘  └──────────────┘
```

## Container Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│              Distributed Ledger System [Container]                   │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │                    Web Layer (REST API)                        │ │
│  │            [Spring Boot, Spring MVC, Spring Security]          │ │
│  └────────────────────────────┬───────────────────────────────────┘ │
│                               │                                      │
│  ┌────────────────────────────▼───────────────────────────────────┐ │
│  │              Application Layer (Use Cases)                     │ │
│  │                [Pure Spring Components]                        │ │
│  └────────────────────────────┬───────────────────────────────────┘ │
│                               │                                      │
│  ┌────────────────────────────▼───────────────────────────────────┐ │
│  │          Domain Layer (Business Logic - THE CORE)              │ │
│  │              [Pure Java, Framework Independent]                │ │
│  └────────────────────────────┬───────────────────────────────────┘ │
│                               │                                      │
│  ┌────────────────────────────▼───────────────────────────────────┐ │
│  │           Infrastructure Layer (Adapters)                      │ │
│  │    [JPA/Hibernate, Kafka Producers/Consumers, Redis Client]   │ │
│  └────────────────────────────────────────────────────────────────┘ │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

## Component Diagram - Domain Layer

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Domain Layer                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │
│  │    Model     │  │    Event     │  │  Exception   │              │
│  │              │  │              │  │              │              │
│  │ • Account    │  │ • Created    │  │ • Domain     │              │
│  │ • Transaction│  │ • Updated    │  │ • Invalid    │              │
│  │ • Money (VO) │  │ • Deleted    │  │ • NotFound   │              │
│  │ • Currency   │  │              │  │              │              │
│  └──────────────┘  └──────────────┘  └──────────────┘              │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                        Ports                                 │    │
│  │  ┌─────────────┐              ┌─────────────┐              │    │
│  │  │   Inbound   │              │  Outbound   │              │    │
│  │  │             │              │             │              │    │
│  │  │ • Use Cases │              │ • Repository│              │    │
│  │  │ • Commands  │              │ • Events    │              │    │
│  │  │ • Queries   │              │ • External  │              │    │
│  │  └─────────────┘              └─────────────┘              │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

## Key Principles

1. **Dependency Rule**: Dependencies point inward (Infrastructure → Application → Domain)
2. **Domain Independence**: Core business logic has no framework dependencies
3. **Testability**: Each layer can be tested independently
4. **Flexibility**: Easy to swap infrastructure components
5. **Clarity**: Clear separation of concerns

## References

- [C4 Model](https://c4model.com/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

