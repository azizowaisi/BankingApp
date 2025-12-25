#!/bin/bash

# Script to create and start the banking database in Docker
# Usage: ./scripts/create-database.sh

set -e

echo "🏦 Banking Database Setup Script"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

echo -e "${BLUE}📦 Starting PostgreSQL database container...${NC}"

# Check if using standalone database compose file
if [ -f "docker-compose.db.yml" ]; then
    echo "Using standalone database configuration..."
    docker-compose -f docker-compose.db.yml up -d postgres
    
    echo ""
    echo -e "${GREEN}✅ Database container started!${NC}"
    echo ""
    echo "Database Details:"
    echo "  - Container: banking-postgres-standalone"
    echo "  - Database: banking_db"
    echo "  - User: banking_user"
    echo "  - Password: banking_pass"
    echo "  - Port: 5432"
    echo ""
    echo "Waiting for database to be ready..."
    
    # Wait for database to be ready
    max_attempts=30
    attempt=0
    while [ $attempt -lt $max_attempts ]; do
        if docker-compose -f docker-compose.db.yml exec -T postgres pg_isready -U banking_user -d banking_db > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Database is ready!${NC}"
            break
        fi
        attempt=$((attempt + 1))
        echo -n "."
        sleep 1
    done
    
    if [ $attempt -eq $max_attempts ]; then
        echo -e "${YELLOW}⚠️  Database took longer than expected to start${NC}"
    fi
    
    echo ""
    echo "Connection string:"
    echo "  jdbc:postgresql://localhost:5432/banking_db"
    echo ""
    echo "To connect via psql:"
    echo "  psql -h localhost -p 5432 -U banking_user -d banking_db"
    echo ""
    echo "Optional: Start pgAdmin at http://localhost:5050"
    echo "  Email: admin@banking.com"
    echo "  Password: admin123"
    
else
    echo "Using main docker-compose.yml..."
    docker-compose up -d postgres
    
    echo ""
    echo -e "${GREEN}✅ Database container started!${NC}"
    echo ""
    echo "Database Details:"
    echo "  - Container: banking-postgres"
    echo "  - Database: banking_db"
    echo "  - User: banking_user"
    echo "  - Password: banking_pass"
    echo "  - Port: 5432"
fi

echo ""
echo -e "${BLUE}📊 Database Status:${NC}"
docker-compose ps postgres 2>/dev/null || docker-compose -f docker-compose.db.yml ps postgres 2>/dev/null

echo ""
echo "Useful commands:"
echo "  - View logs: docker-compose logs -f postgres"
echo "  - Stop database: docker-compose down"
echo "  - Connect to database: docker-compose exec postgres psql -U banking_user -d banking_db"

