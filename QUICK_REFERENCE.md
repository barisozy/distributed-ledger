# Quick Reference: .env Integration

## 📋 Quick Links

| Document | Purpose |
|----------|---------|
| **ENV_CONFIG_GUIDE.md** | 📖 Comprehensive reference guide |
| **ENV_INTEGRATION_SUMMARY.md** | 📋 What was done & how to use |
| **ENV_INTEGRATION_CHECKLIST.md** | ✅ Verification checklist |
| **.env.example** | 📝 Configuration template |

## 🚀 Quick Start

### Local Development
```bash
cp .env.example .env
# Edit .env with your local values
./gradlew bootRun
```

### Docker Compose
```bash
cp .env.example .env
# Edit .env with your values
docker-compose up -d
```

## 🔧 Environment Variables

### Database
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=ledger_db
DB_USER=ledger_user
DB_PASSWORD=your-password
```

### Cache & Messaging
```
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

### Security
```
ENCRYPTION_KEY=v3RyS3cr3tK3yF0rD1stL3dg3rSysT3m!
JWT_SECRET=z5O3P8g1M9qR2sT5vX8yB4nL7jK0hF3cD6aW9eZ2xQ1
JWT_EXPIRATION_MS=86400000
ADMIN_USER=admin
ADMIN_PASSWORD=your-password
```

### Settings
```
LOG_LEVEL=DEBUG
SPRING_PROFILES_ACTIVE=dev
```

## 📁 Modified Files

- ✏️ `application.yml`
- ✏️ `application-dev.yml`
- ✏️ `application-prod.yml`
- ✏️ `docker-compose.yml`

## 📁 Created Files

- ✨ `.env.example` - Configuration template
- ✨ `ENV_CONFIG_GUIDE.md` - Complete guide
- ✨ `ENV_INTEGRATION_SUMMARY.md` - Integration summary
- ✨ `ENV_INTEGRATION_CHECKLIST.md` - Verification checklist
- ✨ `.gitignore-env-template` - Git ignore suggestions

## ⚠️ Important

- ✅ `.env` file contains secrets (do not commit)
- ✅ `.env.example` is safe to commit
- ✅ All defaults have fallback values
- ✅ Production uses profile-specific settings

## 🔍 Verify Setup

```bash
# Check build
./gradlew clean build -x test

# Check configuration
grep -r "ENCRYPTION_KEY\|JWT_SECRET" src/main/resources/

# Test Docker
docker-compose config
```

## ❓ Common Tasks

### Change database
Edit `.env`:
```
DB_HOST=prod-db.example.com
DB_USER=prod_user
DB_PASSWORD=prod_password
```

### Update JWT secret
Edit `.env`:
```
JWT_SECRET=your-new-256-bit-secret-key
```

### Switch to production profile
Edit `.env`:
```
SPRING_PROFILES_ACTIVE=prod
LOG_LEVEL=WARN
```

### Run with Docker
```bash
docker-compose up -d
docker-compose logs -f app
```

## 🆘 Troubleshooting

**Issue**: Connection refused
- Check `DB_HOST` and `DB_PORT` in `.env`
- For Docker: Use service name (e.g., `postgres` not `localhost`)

**Issue**: Encryption key length error
- `ENCRYPTION_KEY` must be exactly 32 characters

**Issue**: JWT secret too short
- `JWT_SECRET` must be at least 44 characters (256 bits)

**Issue**: Variables not loading
- Verify `.env` exists in root directory
- Check file permissions: `chmod 644 .env`
- Restart IDE if using IDE variable loading

## 📚 Documentation

Read full documentation in:
- `ENV_CONFIG_GUIDE.md` - All configuration options explained
- `ENV_INTEGRATION_SUMMARY.md` - Integration details and usage

---

**Status**: ✅ COMPLETE AND READY TO USE

