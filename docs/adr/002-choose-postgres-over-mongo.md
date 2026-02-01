# ADR-002: Choose PostgreSQL Over MongoDB

## Status
Accepted

## Date
2025-12-18

## Context
We need to choose a primary database for the distributed ledger system. The main candidates are:
- **PostgreSQL** (Relational)
- **MongoDB** (Document-based NoSQL)

## Decision
We choose **PostgreSQL** as our primary database.

## Rationale

### Why PostgreSQL?
1. **ACID Compliance**: Critical for financial transactions and ledger consistency
2. **Strong Consistency**: Ledger systems cannot tolerate eventual consistency
3. **Complex Queries**: Support for joins, aggregations, and complex financial reports
4. **JSON Support**: JSONB type provides NoSQL-like flexibility when needed
5. **Proven Track Record**: Battle-tested in financial systems
6. **Excellent Tooling**: Mature ecosystem (Flyway, pgAdmin, monitoring tools)
7. **Row-Level Security**: Built-in security features for multi-tenant scenarios

### Why NOT MongoDB?
1. **Eventual Consistency**: Default behavior unsuitable for ledger operations
2. **Schema Flexibility**: Can lead to data quality issues in financial systems
3. **Transaction Support**: Added later, not as mature as PostgreSQL
4. **Audit Trail**: More complex to implement compared to PostgreSQL triggers

## Consequences

### Positive
- Strong data integrity guarantees
- Better support for complex queries and reports
- Easier to implement audit trails
- Rich ecosystem and community support

### Negative
- Requires careful schema design upfront
- Vertical scaling limits (though sharding possible with Citus)
- ORM overhead (mitigated with proper indexing)

## Technical Details
- Use **Flyway** for database migrations
- Enable **Write-Ahead Logging (WAL)** for replication
- Implement **connection pooling** with HikariCP
- Use **JSONB** columns for flexible metadata storage

## Compliance
- Meets financial data integrity requirements
- Supports comprehensive audit logging
- Enables GDPR compliance with row-level security

