# Distributed Ledger System

A highly scalable, secure distributed ledger system built with hexagonal architecture principles. The system implements a robust money transfer mechanism with optimistic locking, double-entry bookkeeping, and comprehensive audit trails.

## 🏗️ Architecture

This project follows **Hexagonal Architecture (Ports & Adapters)** pattern with clear separation of concerns:

- **Domain Layer**: Pure business logic, framework-agnostic
- **Application Layer**: Use case orchestration
- **Infrastructure Layer**: Framework and external system adapters

### Technology Stack

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.9
- **Database**: PostgreSQL 16 with optimistic locking
- **Cache**: Redis 7
- **Messaging**: Apache Kafka
- **Migration**: Flyway
- **Build**: Gradle 8.x
- **Testing**: JUnit 5, Testcontainers, K6

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Docker & Docker Compose

### Running the Application

1. **Start infrastructure services**:
```bash
docker-compose up -d
```

2. **Build the application**:
```bash
./gradlew build
```

3. **Run the application**:
```bash
./gradlew bootRun
```

### Access Points

- Application: `http://localhost:8080`
- PostgreSQL: `localhost:5432` (user: `ledger_user`, password: `ledger_pass`, db: `distributed_ledger`)
- Redis: `localhost:6379`
- Kafka: `localhost:9092`
- Kafka UI: `http://localhost:8090`

### Testing the API

**Health Check**:
```bash
curl http://localhost:8080/api/v1/health
```

**Send Money** (requires existing accounts in database):
```bash
curl -X POST http://localhost:8080/api/v1/money/send \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountId": "uuid-of-source-account",
    "toAccountId": "uuid-of-target-account",
    "amount": 100.00,
    "currency": "USD",
    "reference": "TXN-001"
  }'
```

## 📁 Project Structure

```
distributed-ledger/
├── docs/
│   ├── adr/                      # Architecture Decision Records
│   │   ├── 001-init-structure.md
│   │   ├── 002-choose-postgres-over-mongo.md
│   │   ├── 003-adoption-of-optimistic-locking.md
│   │   └── 004-implementation-of-mappers.md
│   └── c4-diagrams/              # System architecture diagrams
├── src/main/java/com/distributed/ledger/
│   ├── domain/                   # Core business logic (framework-independent)
│   │   ├── model/                # Entities, Value Objects
│   │   │   ├── Account.java      # Account aggregate with business logic
│   │   │   ├── AccountId.java    # Value object for account identity
│   │   │   ├── AccountStatus.java # Account status enum
│   │   │   ├── Money.java        # Money value object with currency
│   │   │   ├── Transaction.java  # Transaction aggregate
│   │   │   ├── TransactionType.java
│   │   │   └── TransactionStatus.java
│   │   ├── event/                # Domain Events
│   │   │   └── DomainEvent.java
│   │   ├── exception/            # Domain Exceptions
│   │   │   └── DomainException.java
│   │   └── port/                 # Ports (Interfaces)
│   │       ├── in/               # Use Case Interfaces
│   │       │   ├── UseCase.java
│   │       │   ├── SendMoneyUseCase.java
│   │       │   └── SendMoneyCommand.java
│   │       └── out/              # Repository/External Service Interfaces
│   │           ├── LoadAccountPort.java
│   │           ├── SaveAccountPort.java
│   │           └── SaveTransactionPort.java
│   ├── application/              # Use case orchestration
│   │   ├── service/              # Use Case Implementations
│   │   │   └── SendMoneyService.java  # Money transfer with retry logic
│   │   └── dto/                  # Data Transfer Objects
│   │       └── BaseDTO.java
│   └── infrastructure/           # Adapters (Spring Boot, DB, Kafka)
│       ├── adapter/
│       │   ├── web/              # REST Controllers
│       │   │   ├── HealthController.java
│       │   │   ├── SendMoneyController.java
│       │   │   ├── dto/
│       │   │   │   ├── SendMoneyRequest.java
│       │   │   │   └── ApiErrorResponse.java
│       │   │   └── exception/
│       │   │       └── GlobalExceptionHandler.java
│       │   ├── persistence/      # JPA Repositories, Entity Mappers
│       │   │   ├── entity/
│       │   │   │   ├── AccountEntity.java  # JPA entity with @Version
│       │   │   │   └── TransactionEntity.java
│       │   │   ├── mapper/
│       │   │   │   ├── AccountMapper.java
│       │   │   │   └── TransactionMapper.java
│       │   │   ├── repository/
│       │   │   │   ├── SpringDataAccountRepository.java
│       │   │   │   └── SpringDataTransactionRepository.java
│       │   │   ├── AccountPersistenceAdapter.java
│       │   │   └── TransactionPersistenceAdapter.java
│       │   └── messaging/        # Kafka Adapters (placeholder)
│       └── config/               # Spring Configurations
│           ├── RedisConfig.java
│           └── SecurityConfig.java
├── tests/
│   ├── k6/                       # Load test scripts
│   └── integration/              # Integration tests (Testcontainers)
│       ├── IntegrationTestBase.java
│       └── MoneyTransferIntegrationTest.java
└── docker-compose.yml
```

## ✨ Implemented Features

### Domain Model
- ✅ **Account**: Aggregate with deposit/withdraw logic and validation
- ✅ **Money**: Value object with currency and arithmetic operations
- ✅ **Transaction**: Transfer, Deposit, Withdrawal support
- ✅ **Domain Validation**: Business rule enforcement at domain level

### Use Cases
- ✅ **Send Money**: Complete money transfer implementation
  - Atomic withdraw + deposit operations
  - Transaction state management
  - Retry mechanism for optimistic locking failures

### Persistence
- ✅ **Optimistic Locking**: Version-based concurrency control
- ✅ **Double-Entry Bookkeeping**: Schema ready (ledger_entries table)
- ✅ **Audit Log**: Complete audit trail support
- ✅ **Mappers**: Bidirectional domain ↔ entity mapping

### API Endpoints
- ✅ `POST /api/v1/money/send` - Transfer money between accounts
- ✅ `GET /api/v1/health` - Health check endpoint
- ✅ Global exception handling with detailed error responses

### Quality Attributes
- ✅ **Concurrency**: Handled with optimistic locking + @Retryable
- ✅ **ACID Compliance**: @Transactional boundaries
- ✅ **Idempotency**: Transaction reference support
- ✅ **Testing**: Integration tests with Testcontainers

## 🔒 Security & Privacy Manifest

### Security Principles

1. **Defense in Depth**
   - Multi-layered security approach
   - Input validation at all boundaries
   - Output encoding to prevent injection attacks

2. **Least Privilege**
   - Database users have minimal required permissions
   - Service accounts with restricted access
   - Role-based access control (RBAC)

3. **Secure by Default**
   - All endpoints require authentication by default
   - HTTPS enforced in production
   - Secure password policies

### Security Features

#### Authentication & Authorization
- JWT-based authentication
- Role-based access control
- Session management with Redis
- OAuth2/OpenID Connect support

#### Data Protection
- **Encryption at Rest**: PostgreSQL with encryption
- **Encryption in Transit**: TLS 1.3 for all communications
- **Sensitive Data Masking**: PII fields masked in logs
- **Secure Key Management**: External secrets management (Vault/AWS Secrets Manager)

#### Audit & Monitoring
- Comprehensive audit logging
- Transaction traceability
- Anomaly detection
- Real-time security monitoring

### Privacy Compliance

#### GDPR Compliance
- **Right to Access**: User data export functionality
- **Right to Erasure**: Secure data deletion with audit trail
- **Data Minimization**: Only necessary data collected
- **Purpose Limitation**: Clear data usage policies
- **Data Portability**: Standard export formats

#### Data Handling
- Personal data classification
- Retention policies enforced
- Pseudonymization where applicable
- Cross-border transfer safeguards

### Security Best Practices

#### Development
- ✅ No secrets in source code
- ✅ Dependency vulnerability scanning
- ✅ SAST/DAST in CI/CD pipeline
- ✅ Code review mandatory
- ✅ Security testing in development

#### Operations
- ✅ Regular security patches
- ✅ Automated backup and recovery
- ✅ Incident response plan
- ✅ DDoS protection
- ✅ Rate limiting on APIs

### Known Vulnerabilities

Track and remediate vulnerabilities promptly:
- Check `docs/security/cve-tracking.md` for current status
- Run `./gradlew dependencyCheckAnalyze` for security scan

### Reporting Security Issues

**Please DO NOT file public issues for security vulnerabilities.**

Email: security@yourdomain.com

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew integrationTest
```

### Load Tests (k6)
```bash
cd tests/k6
k6 run load-test.js
```

## 📊 Monitoring

- **Metrics**: Micrometer + Prometheus
- **Logging**: SLF4J + Logback
- **Tracing**: OpenTelemetry
- **Health Checks**: Spring Boot Actuator

## 📚 Documentation

### Main Documentation Files
- **[README.md](README.md)** - This file, project overview
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API reference with examples
- **[IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md)** - What's implemented, metrics, status
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - How to contribute
- **[PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md)** - Directory structure and organization
- **[SETUP_COMPLETE.md](SETUP_COMPLETE.md)** - Setup guide and next steps

### Architecture Decision Records
- **[ADR-001](docs/adr/001-init-structure.md)** - Hexagonal architecture
- **[ADR-002](docs/adr/002-choose-postgres-over-mongo.md)** - PostgreSQL choice
- **[ADR-003](docs/adr/003-adoption-of-optimistic-locking.md)** - Optimistic locking strategy
- **[ADR-004](docs/adr/004-implementation-of-mappers.md)** - Mapper implementation

## 📚 Quick Reference

### Key Files to Understand the System

1. **Architecture Decisions**: `docs/adr/` (4 ADRs documenting major decisions)
2. **Domain Models**: 
   - `domain/model/Account.java` - Account aggregate
   - `domain/model/Transaction.java` - Transaction aggregate
   - `domain/model/Money.java` - Money value object
3. **Use Case Implementation**: `application/service/SendMoneyService.java`
4. **REST API**: `infrastructure/adapter/web/SendMoneyController.java`
5. **Database Schema**: `src/main/resources/db/migration/V1__initial_schema.sql`
6. **Mappers**: 
   - `infrastructure/adapter/persistence/mapper/AccountMapper.java`
   - `infrastructure/adapter/persistence/mapper/TransactionMapper.java`
7. **Tests**: `src/test/java/com/distributed/ledger/integration/MoneyTransferIntegrationTest.java`

### Important Concepts

- **Hexagonal Architecture**: Domain is pure Java, infrastructure implements ports
- **Optimistic Locking**: Concurrent updates handled with @Version + @Retryable
- **Value Objects**: Money and AccountId prevent primitive obsession
- **Mappers**: Manual bidirectional conversion between domain and entities
- **ACID Transactions**: @Transactional ensures atomicity

### Directory Quick Navigation

```
domain/               # Business logic - start here
application/service/  # Use case implementations
infrastructure/
  adapter/web/        # REST API endpoints
  adapter/persistence/ # Database layer
    entity/           # JPA entities
    mapper/           # Domain ↔ Entity conversion
    repository/       # Spring Data repositories
docs/adr/            # Architecture decisions - read these!
```

## 🤝 Contributing

1. Read Architecture Decision Records in `docs/adr/`:
   - ADR-001: Hexagonal Architecture structure
   - ADR-002: PostgreSQL vs MongoDB choice
   - ADR-003: Optimistic locking strategy
   - ADR-004: Domain-Entity mapper implementation
2. Follow hexagonal architecture principles
3. Write tests for new features
4. Update documentation (including ADRs for significant changes)
5. Submit PR with clear description

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## 📄 License

[Your License Here]

[Add your license here]

## 📚 Additional Documentation

- [Architecture Decision Records](docs/adr/)
- [API Documentation](docs/api/)
- [Deployment Guide](docs/deployment/)
- [Security Guidelines](docs/security/)

