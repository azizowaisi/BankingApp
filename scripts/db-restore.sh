#!/bin/bash

# Script to restore the banking database from a backup
# Usage: ./scripts/db-restore.sh [backup_file.sql]

set -e

if [ -z "$1" ]; then
    echo "❌ Please provide a backup file"
    echo "Usage: ./scripts/db-restore.sh [backup_file.sql]"
    echo ""
    echo "Available backups:"
    ls -lh ./backend/src/main/resources/db/backups/*.sql 2>/dev/null || echo "No backups found"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "❌ Backup file not found: $BACKUP_FILE"
    exit 1
fi

echo "🔄 Restoring database from backup..."
echo "Backup file: $BACKUP_FILE"
echo ""

# Check if database container is running
if docker ps | grep -q "banking-postgres"; then
    CONTAINER_NAME="banking-postgres"
elif docker ps | grep -q "banking-postgres-standalone"; then
    CONTAINER_NAME="banking-postgres-standalone"
else
    echo "❌ Database container is not running."
    exit 1
fi

read -p "⚠️  This will overwrite the current database. Continue? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Restore cancelled."
    exit 1
fi

echo "Restoring from container: $CONTAINER_NAME"
echo ""

# Copy backup file to container
docker cp "$BACKUP_FILE" "$CONTAINER_NAME:/tmp/backup.sql"

# Restore database
docker exec -i $CONTAINER_NAME psql -U banking_user -d banking_db < "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Database restored successfully!"
else
    echo "❌ Restore failed!"
    exit 1
fi

