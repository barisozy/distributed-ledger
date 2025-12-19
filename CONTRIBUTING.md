# Contributing to Distributed Ledger System

Thank you for your interest in contributing! This document provides guidelines for contributing to the project.

## Architecture Principles

Before contributing, please familiarize yourself with:
1. **Hexagonal Architecture** (Ports & Adapters)
2. **Domain-Driven Design** principles
3. **SOLID principles**
4. **Architecture Decision Records** in `docs/adr/`

## Development Workflow

### 1. Setup Development Environment

```bash
# Clone the repository
git clone <repository-url>
cd distributed-ledger

# Start infrastructure services
docker-compose up -d

# Build the project
./gradlew build

# Run tests
./gradlew test
```

### 2. Branch Naming Convention

- `feature/`: New features (e.g., `feature/add-transaction-service`)
- `bugfix/`: Bug fixes (e.g., `bugfix/fix-balance-calculation`)
- `hotfix/`: Critical production fixes
- `refactor/`: Code refactoring
- `docs/`: Documentation updates

### 3. Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

Example:
```
feat(transaction): add transaction creation endpoint

Implement REST endpoint for creating transactions with
validation and idempotency support.

Closes #123
```

### 4. Coding Standards

#### Package Organization
```
domain/          # Pure business logic, no framework dependencies
  model/         # Entities, Value Objects
    Account.java          # Account aggregate
    AccountId.java        # Identity value object
    Money.java            # Money value object
    Transaction.java      # Transaction aggregate
    TransactionType.java  # TRANSFER, DEPOSIT, WITHDRAWAL
    TransactionStatus.java # PENDING, COMPLETED, FAILED
  event/         # Domain Events
  exception/     # Domain Exceptions
  port/          # Interfaces
    in/          # Use Case Interfaces
      SendMoneyUseCase.java
      SendMoneyCommand.java
    out/         # Repository/External Service Interfaces
      LoadAccountPort.java
      SaveAccountPort.java
      SaveTransactionPort.java

application/     # Use case orchestration
  service/       # Use Case Implementations
    SendMoneyService.java   # Implements @Transactional + @Retryable
  dto/           # Data Transfer Objects

infrastructure/  # Adapters and framework code
  adapter/
    web/         # REST Controllers
      SendMoneyController.java
      dto/
        SendMoneyRequest.java
        ApiErrorResponse.java
      exception/
        GlobalExceptionHandler.java
    persistence/ # Database adapters
      entity/
        AccountEntity.java      # With @Version for optimistic locking
        TransactionEntity.java
      mapper/
        AccountMapper.java      # Domain ↔ Entity conversion
        TransactionMapper.java
      repository/
        SpringDataAccountRepository.java
        SpringDataTransactionRepository.java
      AccountPersistenceAdapter.java
      TransactionPersistenceAdapter.java
    messaging/   # Kafka adapters (future implementation)
  config/        # Spring configurations
```

#### Dependency Rules
- **Domain** → No dependencies (Pure Java)
- **Application** → Domain only
- **Infrastructure** → Application + Domain

#### Code Style
- Use **Java 21** features appropriately (Records, Pattern Matching, Virtual Threads)
- Follow standard Java naming conventions
- Maximum line length: 120 characters
- Use meaningful variable/method names
- Add JavaDoc for public APIs
- Use Lombok for boilerplate reduction (@RequiredArgsConstructor, @Slf4j, etc.)

#### Optimistic Locking Guidelines
- All entities that can be updated concurrently MUST have `@Version` field
- Service methods modifying versioned entities MUST be annotated with `@Retryable`
- Use `@Transactional` for atomicity
- Keep transactions short to minimize contention

#### Mapper Implementation Guidelines (See ADR-004)
- Create manual mapper classes in `infrastructure/adapter/persistence/mapper/`
- Use `@Component` for Spring dependency injection
- Implement both `toDomain(Entity)` and `toEntity(Domain)` methods
- Handle null values explicitly (e.g., nullable fromAccountId in deposits)
- Map value objects correctly (Money, AccountId)
- Preserve version field for optimistic locking
- Example:
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
          entity.setId(domain.getId().value());
          entity.setAccountName(domain.getName());
          entity.setAccountNumber(domain.getAccountNumber());
          entity.setBalance(domain.getBalance().getAmount());
          entity.setCurrency(domain.getBalance().getCurrencyCode());
          entity.setStatus(domain.getStatus().name());
          entity.setVersion(domain.getVersion());
          return entity;
      }
  }
  ```
- Follow standard Java naming conventions
- Maximum line length: 120 characters
- Use meaningful variable/method names
- Add JavaDoc for public APIs

### 5. Testing Requirements

All contributions must include appropriate tests:

#### Unit Tests
```java
@Test
void shouldCalculateBalanceCorrectly() {
    // Given
    Money initial = Money.of(BigDecimal.valueOf(100), "USD");
    Money addition = Money.of(BigDecimal.valueOf(50), "USD");
    
    // When
    Money result = initial.add(addition);
    
    // Then
    assertThat(result.getAmount()).isEqualByComparingTo("150");
}
```

#### Integration Tests
Use Testcontainers for integration tests:
```java
@Testcontainers
@SpringBootTest
class TransactionIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16-alpine");
    
    // Test implementation
}
```

#### Test Coverage
- Minimum coverage: 80%
- Domain layer: 90%+ coverage
- Run: `./gradlew test jacocoTestReport`

### 6. Pull Request Process

1. **Before Creating PR**
   - Update documentation
   - Add/update tests
   - Run full test suite: `./gradlew clean build`
   - Check code style
   - Update CHANGELOG.md

2. **PR Description Template**
   ```markdown
   ## Description
   Brief description of changes
   
   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update
   
   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Integration tests added/updated
   - [ ] Manual testing performed
   
   ## Checklist
   - [ ] Code follows project style guidelines
   - [ ] Self-review completed
   - [ ] Documentation updated
   - [ ] No new warnings
   - [ ] Tests pass locally
   
   ## Related Issues
   Closes #(issue number)
   ```

3. **Review Process**
   - At least one approval required
   - All CI checks must pass
   - No merge conflicts
   - Up-to-date with main branch

### 7. Architecture Decisions

For significant architectural changes:

1. Create an ADR (Architecture Decision Record)
2. Use template: `docs/adr/000-template.md`
3. Number sequentially: `003-your-decision.md`
4. Include in PR

### 8. Security Guidelines

- **Never commit secrets** (use environment variables)
- Validate all inputs
- Use parameterized queries (prevent SQL injection)
- Implement proper authentication/authorization
- Follow OWASP Top 10 guidelines
- Report security issues privately to security@yourdomain.com

### 9. Performance Guidelines

- Use database indexes appropriately
- Implement caching where beneficial
- Avoid N+1 query problems
- Use pagination for large datasets
- Profile before optimizing

### 10. Documentation Requirements

Update documentation for:
- New features
- API changes
- Configuration changes
- Deployment procedures

## Getting Help

- **Questions**: Open a GitHub Discussion
- **Bugs**: Create a GitHub Issue
- **Security**: Email security@yourdomain.com
- **Chat**: [Slack/Discord link]

## Code of Conduct

Be respectful, inclusive, and professional. We follow the [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/).

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

**Thank you for contributing!** 🎉

