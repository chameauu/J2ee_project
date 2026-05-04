# Docker MySQL Setup Guide

This guide explains how to run the Hospital Management System with a real MySQL database using Docker.

---

## Prerequisites

- Docker installed and running
- Docker Compose installed
- Java 17 or higher
- Maven 3.6 or higher

---

## Quick Start

### 1. Start MySQL Database

```bash
# Start MySQL container
docker-compose up -d

# Check if MySQL is running
docker-compose ps

# View MySQL logs
docker-compose logs -f mysql
```

**Wait for MySQL to be ready** (look for "ready for connections" in logs)

### 2. Verify Database Connection

```bash
# Connect to MySQL container
docker exec -it hospital-mysql mysql -u hospital_user -phospital_pass hospital_db

# Inside MySQL, run:
SHOW DATABASES;
USE hospital_db;
SHOW TABLES;
EXIT;
```

### 3. Run Spring Boot Application

```bash
# Option 1: Using Maven with Docker profile
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Option 2: Using default profile (uses root user)
mvn spring-boot:run

# Option 3: Build and run JAR
mvn clean package -DskipTests
java -jar target/hospital-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=docker
```

### 4. Access the Application

- **API**: http://localhost:8080
- **phpMyAdmin**: http://localhost:8081 (optional database UI)
  - Server: `mysql`
  - Username: `root`
  - Password: `root`

---

## Docker Services

### MySQL Database
- **Container**: `hospital-mysql`
- **Port**: 3306
- **Database**: `hospital_db`
- **Root Password**: `root`
- **User**: `hospital_user`
- **Password**: `hospital_pass`

### phpMyAdmin (Optional)
- **Container**: `hospital-phpmyadmin`
- **Port**: 8081
- **URL**: http://localhost:8081

---

## Useful Commands

### Docker Management

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Stop and remove volumes (deletes all data)
docker-compose down -v

# Restart MySQL
docker-compose restart mysql

# View logs
docker-compose logs -f mysql

# Check container status
docker-compose ps
```

### Database Management

```bash
# Connect to MySQL
docker exec -it hospital-mysql mysql -u root -proot hospital_db

# Backup database
docker exec hospital-mysql mysqldump -u root -proot hospital_db > backup.sql

# Restore database
docker exec -i hospital-mysql mysql -u root -proot hospital_db < backup.sql

# View database size
docker exec hospital-mysql mysql -u root -proot -e "SELECT table_schema AS 'Database', ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) AS 'Size (MB)' FROM information_schema.TABLES WHERE table_schema = 'hospital_db' GROUP BY table_schema;"
```

### Application Management

```bash
# Run with Docker profile
mvn spring-boot:run -Dspring-boot.run.profiles=docker

# Run tests (uses H2 in-memory database)
mvn test

# Build without tests
mvn clean package -DskipTests

# Run JAR with Docker profile
java -jar target/hospital-management-0.0.1-SNAPSHOT.jar --spring.profiles.active=docker
```

---

## Database Configuration

### Default Profile (application.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db
spring.datasource.username=root
spring.datasource.password=root
```

### Docker Profile (application-docker.properties)
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db
spring.datasource.username=hospital_user
spring.datasource.password=hospital_pass
```

---

## Troubleshooting

### MySQL Container Won't Start

```bash
# Check if port 3306 is already in use
lsof -i :3306

# If MySQL is already running locally, stop it
sudo systemctl stop mysql  # Linux
brew services stop mysql   # macOS

# Or change the port in docker-compose.yml
ports:
  - "3307:3306"  # Use port 3307 instead
```

### Connection Refused Error

```bash
# Wait for MySQL to be fully ready
docker-compose logs -f mysql

# Look for: "ready for connections"

# Check MySQL health
docker exec hospital-mysql mysqladmin ping -h localhost -u root -proot
```

### Tables Not Created

```bash
# Check Hibernate logs in Spring Boot console
# Look for: "Hibernate: create table..."

# Verify spring.jpa.hibernate.ddl-auto=update in application.properties

# Manually check tables
docker exec -it hospital-mysql mysql -u root -proot hospital_db -e "SHOW TABLES;"
```

### Permission Denied

```bash
# Grant all privileges to hospital_user
docker exec -it hospital-mysql mysql -u root -proot -e "GRANT ALL PRIVILEGES ON hospital_db.* TO 'hospital_user'@'%'; FLUSH PRIVILEGES;"
```

### Reset Everything

```bash
# Stop and remove all containers and volumes
docker-compose down -v

# Remove MySQL data volume
docker volume rm project_mysql_data

# Start fresh
docker-compose up -d
```

---

## Testing with Real Database

### 1. Start MySQL
```bash
docker-compose up -d
```

### 2. Run Application
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

### 3. Test API Endpoints

```bash
# Health check
curl http://localhost:8080/actuator/health

# Register a user (example)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@hospital.com",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@hospital.com",
    "password": "admin123"
  }'
```

### 4. Verify Data in Database

```bash
# Connect to MySQL
docker exec -it hospital-mysql mysql -u root -proot hospital_db

# Check tables
SHOW TABLES;

# Check users
SELECT * FROM users;

# Check hospitals
SELECT * FROM hospitals;
```

---

## Production Considerations

### Security
- Change default passwords in production
- Use environment variables for sensitive data
- Enable SSL for MySQL connections
- Restrict network access

### Performance
- Adjust connection pool settings
- Add database indexes
- Monitor query performance
- Use read replicas for scaling

### Backup
- Set up automated backups
- Test restore procedures
- Store backups securely
- Implement point-in-time recovery

### Monitoring
- Monitor database metrics
- Set up alerts for errors
- Track slow queries
- Monitor disk space

---

## Next Steps

1. ✅ Start MySQL with Docker
2. ✅ Run Spring Boot application
3. ✅ Test API endpoints
4. ✅ Verify data in database
5. 🚀 Deploy to production

---

**Created**: May 4, 2026  
**Status**: Ready for testing  
**MySQL Version**: 9.0.0  
**Spring Boot Version**: 3.5.13
