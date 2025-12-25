#!/bin/bash

# Script to backup the banking database
# Usage: ./scripts/db-backup.sh

set -e

TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="./backend/src/main/resources/db/backups"
BACKUP_FILE="$BACKUP_DIR/banking_db_backup_$TIMESTAMP.sql"

echo "💾 Creating database backup..."
echo ""

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Check if database container is running
if docker ps | grep -q "banking-postgres"; then
    CONTAINER_NAME="banking-postgres"
elif docker ps | grep -q "banking-postgres-standalone"; then
    CONTAINER_NAME="banking-postgres-standalone"
else
    echo "❌ Database container is not running."
    exit 1
fi

echo "Backing up database from container: $CONTAINER_NAME"
echo "Backup file: $BACKUP_FILE"
echo ""

docker exec -t $CONTAINER_NAME pg_dump -U banking_user -d banking_db > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Backup created successfully: $BACKUP_FILE"
    echo "File size: $(du -h "$BACKUP_FILE" | cut -f1)"
else
    echo "❌ Backup failed!"
    exit 1
fi

