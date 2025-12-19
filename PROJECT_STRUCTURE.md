# Project Structure Overview

## Complete Directory Layout

```
distributed-ledger/
├── docs/
│   ├── adr/                                      # Architecture Decision Records
│   │   ├── 001-init-structure.md                 # Hexagonal architecture adoption
│   │   ├── 002-choose-postgres-over-mongo.md     # Database selection rationale
│   │   ├── 003-adoption-of-optimistic-locking.md # Concurrency strategy
│   │   └── 004-implementation-of-mappers.md      # Domain-Entity mapping approach
│   └── c4-diagrams/                              # System architecture diagrams
│       └── system-architecture.md
│
├── src/
│   ├── main/
│   │   ├── java/com/distributed/ledger/
│   │   │   ├── domain/                           # CORE - Pure Business Logic
│   │   │   │   ├── model/                        # Domain Models
│   │   │   │   │   ├── Account.java              # ✅ Account aggregate with business rules
│   │   │   │   │   ├── AccountId.java            # ✅ Value object for account identity
│   │   │   │   │   ├── AccountStatus.java        # ✅ ACTIVE, SUSPENDED, CLOSED
│   │   │   │   │   ├── Money.java                # ✅ Value object with currency operations
│   │   │   │   │   ├── Transaction.java          # ✅ Transaction aggregate
│   │   │   │   │   ├── TransactionType.java      # ✅ TRANSFER, DEPOSIT, WITHDRAWAL
│   │   │   │   │   └── TransactionStatus.java    # ✅ PENDING, COMPLETED, FAILED
│   │   │   │   ├── event/                        # Domain Events
│   │   │   │   │   └── DomainEvent.java          # Base interface for events
│   │   │   │   ├── exception/                    # Domain Exceptions
│   │   │   │   │   └── DomainException.java      # Base domain exception
│   │   │   │   └── port/                         # Ports (Interfaces)
│   │   │   │       ├── in/                       # Inbound Ports (Use Cases)
│   │   │   │       │   ├── UseCase.java
│   │   │   │       │   ├── SendMoneyUseCase.java # ✅ Money transfer contract
│   │   │   │       │   └── SendMoneyCommand.java # ✅ Command DTO
│   │   │   │       └── out/                      # Outbound Ports (External Services)
│   │   │   │           ├── LoadAccountPort.java  # ✅ Account loading
│   │   │   │           ├── SaveAccountPort.java  # ✅ Account persistence
│   │   │   │           └── SaveTransactionPort.java # ✅ Transaction persistence
│   │   │   │
│   │   │   ├── application/                      # USE CASE ORCHESTRATION
│   │   │   │   ├── service/                      # Use Case Implementations
│   │   │   │   │   └── SendMoneyService.java     # ✅ Transfer logic with @Retryable
│   │   │   │   └── dto/                          # Data Transfer Objects
│   │   │   │       └── BaseDTO.java
│   │   │   │
│   │   │   └── infrastructure/                   # ADAPTERS - Framework & External
│   │   │       ├── DistributedLedgerApplication.java  # Spring Boot Main Class
│   │   │       ├── adapter/
│   │   │       │   ├── web/                      # REST Controllers
│   │   │       │   │   ├── HealthController.java # ✅ Health endpoint
│   │   │       │   │   ├── SendMoneyController.java # ✅ Money transfer API
│   │   │       │   │   ├── dto/
│   │   │       │   │   │   ├── SendMoneyRequest.java # ✅ API request DTO
│   │   │       │   │   │   └── ApiErrorResponse.java # ✅ Error response DTO
│   │   │       │   │   └── exception/
│   │   │       │   │       └── GlobalExceptionHandler.java # ✅ Exception handling
│   │   │       │   ├── persistence/              # JPA/Database Adapters
│   │   │       │   │   ├── entity/
│   │   │       │   │   │   ├── AccountEntity.java # ✅ JPA entity with @Version
│   │   │       │   │   │   └── TransactionEntity.java # ✅ JPA entity
│   │   │       │   │   ├── mapper/
│   │   │       │   │   │   ├── AccountMapper.java # ✅ Domain ↔ Entity mapper
│   │   │       │   │   │   └── TransactionMapper.java # ✅ Domain ↔ Entity mapper
│   │   │       │   │   ├── repository/
│   │   │       │   │   │   ├── SpringDataAccountRepository.java # ✅ JPA repository
│   │   │       │   │   │   └── SpringDataTransactionRepository.java # ✅ JPA repository
│   │   │       │   │   ├── AccountPersistenceAdapter.java # ✅ Port implementation
│   │   │       │   │   └── TransactionPersistenceAdapter.java # ✅ Port implementation
│   │   │       │   └── messaging/                # Kafka Adapters
│   │   │       │       └── (Future implementation)
│   │   │       └── config/                       # Spring Configurations
│   │   │           ├── RedisConfig.java
│   │   │           └── SecurityConfig.java
│   │   │
│   │   └── resources/
│   │       ├── db/migration/                     # Database Migration Scripts
│   │       │   └── V1__initial_schema.sql        # ✅ Complete schema with audit
│   │       ├── application.yml                   # Main configuration
│   │       ├── application-dev.yml               # Development profile
│   │       └── application-prod.yml              # Production profile
│   │
│   └── test/
│       └── java/com/distributed/ledger/
│           ├── DistributedLedgerApplicationTests.java
│           └── integration/
│               ├── IntegrationTestBase.java      # ✅ Test base with Testcontainers
│               └── MoneyTransferIntegrationTest.java # ✅ E2E transfer test
│
├── tests/
│   ├── integration/                              # Integration Tests
│   └── k6/                                       # Load Tests (k6)
│       ├── load-test.js
│       └── README.md
│
├── docker-compose.yml                            # ✅ PostgreSQL, Redis, Kafka
├── build.gradle                                  # ✅ Complete dependencies
├── settings.gradle                               # Gradle Settings
├── gradlew                                       # Gradle Wrapper (Unix)
├── gradlew.bat                                   # Gradle Wrapper (Windows)
├── .gitignore                                    # Git Ignore Rules
├── README.md                                     # ✅ Updated documentation
├── CONTRIBUTING.md                               # ✅ Updated guidelines
├── PROJECT_STRUCTURE.md                          # ✅ This file
└── SETUP_COMPLETE.md                            # ✅ Setup guide
```

## Layer Responsibilities

### 🎯 Domain Layer (Core)
**Location**: `src/main/java/com/distributed/ledger/domain/`

**Purpose**: Pure business logic, framework-independent

**Contains**:
- **model/**: Entities, Value Objects (e.g., Money, Currency)
- **event/**: Domain Events (business events that happened)
- **exception/**: Domain-specific exceptions
- **port/in/**: Inbound ports (use case interfaces)
- **port/out/**: Outbound ports (repository/external service interfaces)

**Rules**:
- ✅ NO framework dependencies (no Spring, no JPA)
- ✅ Pure Java only
- ✅ Business rules live here
- ✅ Most heavily tested layer

### 🔄 Application Layer
**Location**: `src/main/java/com/distributed/ledger/application/`

**Purpose**: Orchestrate use cases and business workflows

**Contains**:
- **service/**: Use case implementations
- **dto/**: Data Transfer Objects

**Rules**:
- ✅ Can depend on Domain layer
- ✅ Uses ports to interact with external systems
- ✅ Coordinates domain objects
- ❌ No direct framework dependencies (prefer standard Java)

### 🔌 Infrastructure Layer
**Location**: `src/main/java/com/distributed/ledger/infrastructure/`

**Purpose**: Technical implementations and framework integration

**Contains**:
- **adapter/web/**: REST API controllers (Spring MVC)
- **adapter/persistence/**: Database implementations (JPA, Hibernate)
- **adapter/messaging/**: Message broker implementations (Kafka)
- **config/**: Spring configuration classes
- **DistributedLedgerApplication.java**: Spring Boot main class

**Rules**:
- ✅ Can depend on Application and Domain layers
- ✅ Framework dependencies allowed (Spring Boot, JPA, Kafka)
- ✅ Implements outbound ports
- ✅ Calls inbound ports

## Dependency Flow

```
Infrastructure → Application → Domain
       ↓
   [External]
   - Database
   - Kafka
   - Redis
```

## Key Files

### Configuration
- **application.yml**: Main application configuration
- **docker-compose.yml**: Local development infrastructure
- **build.gradle**: Project dependencies and build configuration

### Documentation
- **docs/adr/**: Architecture decisions (CRITICAL for understanding "why")
- **docs/c4-diagrams/**: Visual architecture documentation
- **README.md**: Project overview and getting started
- **CONTRIBUTING.md**: Contribution guidelines

### Database
- **db/migration/V1__initial_schema.sql**: Initial database schema (Flyway)

### Testing
- **tests/k6/load-test.js**: Performance testing script
- **tests/integration/**: Integration tests with Testcontainers

## Quick Commands

```bash
# Start infrastructure
docker-compose up -d

# Build project
./gradlew build

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Run integration tests (requires Docker)
./gradlew test --tests "*IntegrationTest"

# Check for dependency vulnerabilities
./gradlew dependencyCheckAnalyze

# Stop infrastructure
docker-compose down
```

## Implementation Status

### ✅ Completed Features

1. **Domain Layer**
   - ✅ Account aggregate with deposit/withdraw logic
   - ✅ Money value object with currency support
   - ✅ Transaction aggregate with state management
   - ✅ Complete port definitions (in/out)
   - ✅ Domain validation and exception handling

2. **Application Layer**
   - ✅ SendMoneyService with @Transactional + @Retryable
   - ✅ Use case orchestration
   - ✅ Command pattern implementation

3. **Infrastructure Layer**
   - ✅ REST API endpoints (SendMoney, Health)
   - ✅ JPA entities with @Version for optimistic locking
   - ✅ Complete mappers (bidirectional domain ↔ entity)
   - ✅ Spring Data repositories
   - ✅ Persistence adapters implementing ports
   - ✅ Global exception handler

4. **Database**
   - ✅ Complete schema with accounts, transactions, ledger_entries
   - ✅ Audit log table
   - ✅ Indexes for performance
   - ✅ Triggers for updated_at
   - ✅ Flyway migration

5. **Testing**
   - ✅ Integration test base with Testcontainers
   - ✅ MoneyTransferIntegrationTest (E2E test)
   - ✅ K6 load test scripts

### 🚧 In Progress / Future Work

1. **Domain Events**
   - Implement TransactionCreated, AccountUpdated events
   - Event sourcing consideration

2. **Messaging Layer**
   - Kafka producers for domain events
   - Kafka consumers for event processing
   - Event-driven architecture patterns

3. **Additional Use Cases**
   - Query account balance
   - Query transaction history
   - Create new account
   - Deposit money
   - Withdraw money

4. **Security**
   - JWT authentication
   - Role-based authorization
   - API key management

5. **Observability**
   - Prometheus metrics
   - Distributed tracing
   - Structured logging

6. **Advanced Features**
   - Double-entry bookkeeping implementation
   - Multi-currency support enhancements
   - Transaction rollback/compensation
   - Scheduled transfers

## Package Naming Conventions

- `model`: Domain entities and value objects
- `event`: Domain events
- `exception`: Custom exceptions
- `port.in`: Use case interfaces (commands, queries)
- `port.out`: Repository and external service interfaces
- `service`: Use case implementations
- `dto`: Data transfer objects
- `adapter.web`: REST controllers
- `adapter.persistence`: Database implementations
- `adapter.messaging`: Message broker implementations
- `config`: Configuration classes

---

**Remember**: The domain layer is the heart of the application. Keep it pure, testable, and free from framework dependencies!

