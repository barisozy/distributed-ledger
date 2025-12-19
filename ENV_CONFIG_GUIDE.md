# Environment Configuration Guide

## Overview

This project uses environment variables for configuration management. The `.env` file contains all necessary configuration values for both local development and production environments.

## File Structure

- **`.env`** - Main environment configuration file (DO NOT commit to git - add to .gitignore)
- **`.env.example`** - Example configuration file for documentation (safe to commit)
- **`application.yml`** - Main Spring Boot configuration with fallback defaults
- **`application-dev.yml`** - Development profile configuration
- **`application-prod.yml`** - Production profile configuration
- **`docker-compose.yml`** - Docker Compose configuration that loads `.env`

## Configuration Categories

### 1. Database Configuration (PostgreSQL)

```yaml
DB_HOST=localhost          # Database host
DB_PORT=5432              # Database port
DB_NAME=ledger_db         # Database name
DB_USER=ledger_user       # Database user
DB_PASSWORD=secret        # Database password
```

**Used in:**
- `application.yml` → Spring datasource configuration
- `application-dev.yml` → Development datasource
- `application-prod.yml` → Production datasource
- `docker-compose.yml` → PostgreSQL service environment

### 2. Redis Configuration

```yaml
REDIS_HOST=localhost      # Redis host
REDIS_PORT=6379          # Redis port
REDIS_PASSWORD=          # Redis password (optional)
```

**Used in:**
- `application.yml` → Spring Data Redis configuration
- `application-dev.yml` → Development cache configuration
- `application-prod.yml` → Production cache configuration
- `docker-compose.yml` → Redis service port mapping

### 3. Kafka Configuration

```yaml
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**Used in:**
- `application.yml` → Spring Kafka bootstrap servers
- `application-dev.yml` → Development Kafka consumer group
- `application-prod.yml` → Production Kafka producer settings
- `docker-compose.yml` → Kafka service bootstrap server

### 4. Security & Encryption

#### Encryption Key (AES-256)
```yaml
ENCRYPTION_KEY=v3RyS3cr3tK3yF0rD1stL3dg3rSysT3m!
```
- **Length:** Must be exactly 32 characters for AES-256
- **Purpose:** Encrypts PII data in the database
- **Used in:** All application profiles

#### JWT Configuration
```yaml
JWT_SECRET=z5O3P8g1M9qR2sT5vX8yB4nL7jK0hF3cD6aW9eZ2xQ1
JWT_EXPIRATION_MS=86400000
```
- **JWT_SECRET:** Signing key for JWT tokens (minimum 256-bit recommended)
- **JWT_EXPIRATION_MS:** Token expiration in milliseconds (default: 24 hours)
- **Used in:** All application profiles

#### Admin Credentials
```yaml
ADMIN_USER=admin
ADMIN_PASSWORD=super-secure-admin-password
```
- **Used for:** Default admin user creation and authentication
- **Used in:** All application profiles

### 5. Application Settings

```yaml
LOG_LEVEL=DEBUG
SPRING_PROFILES_ACTIVE=dev
```
- **LOG_LEVEL:** Logging verbosity (DEBUG, INFO, WARN, ERROR)
- **SPRING_PROFILES_ACTIVE:** Active Spring profile (dev or prod)

## How Environment Variables Are Resolved

### Priority Order (Spring Boot)

1. **Command-line arguments** (highest priority)
   ```bash
   java -jar app.jar --server.port=8080
   ```

2. **System environment variables**
   ```bash
   export DB_HOST=prod-db.example.com
   ```

3. **`.env` file** (loaded by Docker Compose or IDE)
   ```
   DB_HOST=localhost
   ```

4. **Application YAML defaults** (lowest priority)
   ```yaml
   DB_HOST: ${DB_HOST:localhost}  # Falls back to 'localhost'
   ```

## Usage Scenarios

### Local Development (IDE)

1. Create a `.env` file based on `.env.example`
2. Configure IDE to load `.env` file (IntelliJ IDEA: Run → Edit Configurations → Environment variables)
3. Run the application with `SPRING_PROFILES_ACTIVE=dev`
4. Database connection: `jdbc:postgresql://localhost:5432/ledger_db`

### Local Development (Docker Compose)

1. Create a `.env` file based on `.env.example`
2. Run: `docker-compose up`
3. Docker Compose automatically loads `.env` file
4. Service connections use internal Docker hostnames (e.g., `postgres`, `redis`, `kafka`)
5. Database connection inside container: `jdbc:postgresql://postgres:5432/ledger_db`

### Production Deployment

1. Set environment variables on the server/container orchestration platform:
   ```bash
   export DB_HOST=prod-db.example.com
   export DB_USER=prod_user
   export DB_PASSWORD=prod_secure_password
   export ENCRYPTION_KEY=prod_encryption_key_32_chars!
   ```

2. Deploy application (Kubernetes, Docker, etc.)
3. Application loads with `SPRING_PROFILES_ACTIVE=prod`
4. Production profile enforces strict validation and no SQL debugging

## Security Best Practices

### DO ✅

- Store sensitive values in `.env` (local development only)
- Use different secrets for each environment
- Never commit `.env` to version control
- Use strong, random passwords and encryption keys
- Document all configuration options in `.env.example`
- Rotate credentials regularly in production
- Use secrets management systems (AWS Secrets Manager, HashiCorp Vault) in production

### DON'T ❌

- Commit `.env` file to git
- Use weak or default credentials
- Hardcode secrets in code or YAML files
- Share `.env` file publicly
- Use the same secrets across environments
- Log sensitive values

## Configuration File Synchronization

All configuration files are synchronized to use the same environment variables:

```
.env (source) ↓
    ├→ application.yml (base configuration)
    ├→ application-dev.yml (dev profile)
    ├→ application-prod.yml (prod profile)
    └→ docker-compose.yml (Docker services)
```

When you update `.env`:
1. All profiles automatically load the new values
2. Spring Boot resolves placeholders like `${DB_HOST}`
3. Docker Compose environment variables are updated on restart

## Troubleshooting

### Issue: "Connection refused" for database

**Solution:**
1. Check `DB_HOST` and `DB_PORT` in `.env`
2. Verify PostgreSQL is running on the correct host/port
3. Ensure firewall allows connections
4. In Docker: Use service name (e.g., `postgres`) not `localhost`

### Issue: "JWT secret is too short"

**Solution:**
- Ensure `JWT_SECRET` is at least 256 bits
- Minimum recommended length: 44 characters (Base64 encoded 32 bytes)

### Issue: "Encryption key has wrong length"

**Solution:**
- `ENCRYPTION_KEY` must be exactly 32 characters for AES-256
- Use a strong random key generator

### Issue: Environment variables not loading

**Solution:**
1. Verify `.env` file exists in the root directory
2. Check file permissions: `chmod 644 .env`
3. Verify IDE is configured to load `.env`
4. For Docker: Ensure `docker-compose.yml` includes `env_file: - .env`

## References

- [Spring Boot Documentation - Environment Variables](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Docker Compose Environment Files](https://docs.docker.io/compose/environment-variables/)
- [12-Factor App - Store Configuration in Environment](https://12factor.net/config)

