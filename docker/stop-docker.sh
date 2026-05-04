#!/bin/bash

# Hospital Management System - Docker Stop Script

set -e

echo "🏥 Hospital Management System - Stopping Docker Services"
echo "========================================================="
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Ask if user wants to remove volumes (data)
echo -e "${YELLOW}Do you want to remove all data (volumes)? (y/n)${NC}"
echo "  - No: Keep database data (can restart later)"
echo "  - Yes: Delete all database data (fresh start)"
read -r response

if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo ""
    echo "🗑️  Stopping containers and removing volumes..."
    docker compose down -v
    echo -e "${GREEN}✅ All containers and data removed${NC}"
else
    echo ""
    echo "⏸️  Stopping containers (keeping data)..."
    docker compose down
    echo -e "${GREEN}✅ Containers stopped (data preserved)${NC}"
    echo ""
    echo "💡 To restart: ./start-docker.sh or docker-compose up -d"
fi

echo ""
