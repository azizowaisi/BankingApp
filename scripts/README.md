# Database Management Scripts

This directory contains scripts to manage the banking database in Docker.

## Available Scripts

### 1. `create-database.sh`
Creates and starts the PostgreSQL database container.

**Usage:**
```bash
./scripts/create-database.sh
```

**What it does:**
- Starts PostgreSQL container
- Waits for database to be ready
- Displays connection information
- Shows useful commands

### 2. `db-connect.sh`
Connects to the database using psql.

**Usage:**
```bash
./scripts/db-connect.sh
```

**What it does:**
- Finds the running database container
- Opens interactive psql session
- Connects as `banking_user` to `banking_db`

### 3. `db-backup.sh`
Creates a backup of the database.

**Usage:**
```bash
./scripts/db-backup.sh
```

**What it does:**
- Creates timestamped SQL backup
- Saves to `backend/src/main/resources/db/backups/`
- Shows backup file location and size

### 4. `db-restore.sh`
Restores database from a backup file.

**Usage:**
```bash
./scripts/db-restore.sh [backup_file.sql]
```

**What it does:**
- Lists available backups
- Prompts for confirmation
- Restores database from backup file

## Quick Start

### Option 1: Standalone Database (Recommended for Development)

```bash
# Start only the database
docker-compose -f docker-compose.db.yml up -d

# Or use the script
./scripts/create-database.sh
```

This starts:
- PostgreSQL database on port 5432
- pgAdmin (optional) on port 5050

### Option 2: Full Stack

```bash
# Start all services (database + backend + frontend)
docker-compose up -d
```

## Database Connection

### From Host Machine

```bash
psql -h localhost -p 5432 -U banking_user -d banking_db
# Password: banking_pass
```

### Using Script

```bash
./scripts/db-connect.sh
```

### Connection String

```
jdbc:postgresql://localhost:5432/banking_db
Username: banking_user
Password: banking_pass
```

## pgAdmin (Web Interface)

If using `docker-compose.db.yml`, pgAdmin is available at:

- **URL**: http://localhost:5050
- **Email**: admin@banking.com
- **Password**: admin123

### Adding Server in pgAdmin

1. Right-click "Servers" → "Create" → "Server"
2. General tab:
   - Name: Banking DB
3. Connection tab:
   - Host: postgres (or localhost if connecting from host)
   - Port: 5432
   - Database: banking_db
   - Username: banking_user
   - Password: banking_pass

## Useful Commands

### View Database Logs

```bash
docker-compose logs -f postgres
# or
docker logs -f banking-postgres-standalone
```

### Stop Database

```bash
docker-compose down
# or for standalone
docker-compose -f docker-compose.db.yml down
```

### Remove Database and Data

```bash
docker-compose down -v
# This removes volumes (deletes all data!)
```

### Check Database Status

```bash
docker-compose ps postgres
```

### Execute SQL Commands

```bash
docker exec -it banking-postgres psql -U banking_user -d banking_db
```

### List Databases

```bash
docker exec banking-postgres psql -U banking_user -l
```

### List Tables

```bash
docker exec banking-postgres psql -U banking_user -d banking_db -c "\dt"
```

## Backup and Restore

### Create Backup

```bash
./scripts/db-backup.sh
```

Backups are saved to: `backend/src/main/resources/db/backups/`

### Restore from Backup

```bash
./scripts/db-restore.sh backend/src/main/resources/db/backups/banking_db_backup_20240101_120000.sql
```

## Troubleshooting

### Database Won't Start

1. Check if port 5432 is already in use:
   ```bash
   lsof -i :5432
   ```

2. Check Docker logs:
   ```bash
   docker-compose logs postgres
   ```

3. Remove old container and start fresh:
   ```bash
   docker-compose down -v
   docker-compose up -d postgres
   ```

### Connection Refused

1. Verify container is running:
   ```bash
   docker ps | grep postgres
   ```

2. Check health status:
   ```bash
   docker-compose ps postgres
   ```

3. Wait for database to be ready (may take 10-30 seconds)

### Permission Denied

Make scripts executable:
```bash
chmod +x scripts/*.sh
```

## Database Schema

Tables are automatically created by JPA/Hibernate when the backend starts:

- `users` - User accounts
- `accounts` - Bank accounts
- `transactions` - Money transfers
- `audit_logs` - System audit trail
- `fraud_events` - Fraud detection events

To view schema:
```bash
./scripts/db-connect.sh
\dt
\d+ users
\d+ accounts
# etc.
```

