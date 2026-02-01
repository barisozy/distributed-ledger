# ADR-004: Implementation of Domain-Entity Mappers

Date: 2025-12-19
Status: Accepted

## Context
In a hexagonal architecture, the domain layer must remain framework-agnostic, while the infrastructure layer uses JPA entities. We need a clean way to convert between:
1. **Domain models** (pure Java, business logic)
2. **JPA entities** (persistence framework, database representation)

Two approaches were considered:
1. **Manual Mappers**: Custom mapper classes with explicit conversion logic
2. **MapStruct**: Annotation-based automatic mapping generation

## Decision
We implemented **manual mapper classes** (`AccountMapper` and `TransactionMapper`) in the infrastructure layer.

## Rationale

### Why Manual Mappers?

1. **Explicit Control**: Full control over conversion logic, especially for complex value objects like `Money` and `AccountId`
2. **No Magic**: Clear, readable code without annotation processing complexity
3. **Type Safety**: Compile-time checking without code generation
4. **Flexibility**: Easy to handle special cases (null handling, bidirectional mapping)
5. **Testability**: Simple to unit test mapper behavior
6. **Learning Curve**: Easier for team members to understand and maintain

### Why NOT MapStruct?

1. **Overkill for Simple Cases**: Our mappings are straightforward
2. **Build Complexity**: Additional annotation processor configuration
3. **Debugging**: Generated code can be harder to debug
4. **Custom Logic**: We need custom logic for value objects anyway

## Implementation Details

### AccountMapper Example
```java
@Component
public class AccountMapper {
    public Account toDomain(AccountEntity entity) {
        return Account.with(
            AccountId.of(entity.getId()),
            entity.getAccountName(),
            entity.getAccountNumber(),
            Money.of(entity.getBalance(), entity.getCurrency()),
            AccountStatus.valueOf(entity.getStatus()),
            entity.getVersion()
        );
    }

    public AccountEntity toEntity(Account domain) {
        AccountEntity entity = new AccountEntity();
        // ... explicit field mapping
        return entity;
    }
}
```

### Key Mapping Challenges Solved

1. **Value Objects**: Money, AccountId require explicit construction
2. **Enums**: String ↔ Enum conversion with validation
3. **Nullability**: Proper handling of optional fields (fromAccountId in deposits)
4. **Bidirectional Mapping**: Both toDomain() and toEntity() methods
5. **Version Field**: Optimistic locking version propagation

## Consequences

### Positive
- ✅ Clear, maintainable code
- ✅ Easy to debug and test
- ✅ No build-time code generation
- ✅ Full type safety
- ✅ Simple dependency injection with @Component

### Negative
- ⚠️ Manual updates when domain/entity changes (mitigated by tests)
- ⚠️ Slightly more boilerplate (acceptable tradeoff)

### Neutral
- Mappers reside in `infrastructure/adapter/persistence/mapper/` package
- Mappers are Spring-managed beans (@Component)
- One mapper per aggregate root (Account, Transaction)

## Compliance
- ✅ Preserves hexagonal architecture boundaries
- ✅ Domain remains framework-agnostic
- ✅ Infrastructure depends on domain, not vice versa
- ✅ Testable in isolation

## Future Considerations
If mapping logic becomes significantly complex (e.g., 10+ fields with transformations), we can reconsider MapStruct or similar tools.

## Related
- ADR-001: Hexagonal architecture structure
- ADR-003: Optimistic locking (version field mapping)

