#!/bin/bash

# Script to connect to the banking database
# Usage: ./scripts/db-connect.sh

set -e

echo "🔌 Connecting to Banking Database..."
echo ""

# Check if database container is running
if docker ps | grep -q "banking-postgres"; then
    CONTAINER_NAME="banking-postgres"
elif docker ps | grep -q "banking-postgres-standalone"; then
    CONTAINER_NAME="banking-postgres-standalone"
else
    echo "❌ Database container is not running."
    echo "Start it with: ./scripts/create-database.sh"
    exit 1
fi

echo "Connecting to container: $CONTAINER_NAME"
echo "Database: banking_db"
echo "User: banking_user"
echo ""

docker exec -it $CONTAINER_NAME psql -U banking_user -d banking_db

