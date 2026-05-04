#!/bin/bash

# Hospital Management System - Docker Startup Script
# This script starts MySQL with Docker and runs the Spring Boot application

set -e

echo "🏥 Hospital Management System - Docker Setup"
echo "=============================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker is running${NC}"
echo ""

# Start MySQL container
echo "📦 Starting MySQL container..."
docker compose up -d

echo ""
echo "⏳ Waiting for MySQL to be ready..."

# Wait for MySQL to be healthy
MAX_ATTEMPTS=30
ATTEMPT=0

while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    if docker exec hospital-mysql mysqladmin ping -h localhost -u root -proot --silent > /dev/null 2>&1; then
        echo -e "${GREEN}✅ MySQL is ready!${NC}"
        break
    fi
    
    ATTEMPT=$((ATTEMPT + 1))
    echo -n "."
    sleep 2
    
    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo -e "${RED}❌ MySQL failed to start after ${MAX_ATTEMPTS} attempts${NC}"
        echo "Check logs with: docker-compose logs mysql"
        exit 1
    fi
done

echo ""
echo "🔍 Verifying database..."

# Verify database exists
if docker exec hospital-mysql mysql -u root -proot -e "USE hospital_db;" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Database 'hospital_db' is ready${NC}"
else
    echo -e "${RED}❌ Database 'hospital_db' not found${NC}"
    exit 1
fi

echo ""
echo "📊 Database Information:"
echo "  - Host: localhost:3306"
echo "  - Database: hospital_db"
echo "  - User: hospital_user"
echo "  - Password: hospital_pass"
echo "  - Root Password: root"
echo ""
echo "🌐 phpMyAdmin: http://localhost:8081"
echo ""

# Ask if user wants to start Spring Boot
echo -e "${YELLOW}Do you want to start the Spring Boot application now? (y/n)${NC}"
read -r response

if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
    echo ""
    echo "🚀 Starting Spring Boot application..."
    echo ""
    mvn spring-boot:run -Dspring-boot.run.profiles=docker
else
    echo ""
    echo -e "${GREEN}✅ MySQL is running. You can start the application manually with:${NC}"
    echo "   mvn spring-boot:run -Dspring-boot.run.profiles=docker"
    echo ""
    echo "📝 Useful commands:"
    echo "   - View logs: docker-compose logs -f mysql"
    echo "   - Stop MySQL: docker-compose down"
    echo "   - Connect to MySQL: docker exec -it hospital-mysql mysql -u root -proot hospital_db"
    echo ""
fi
