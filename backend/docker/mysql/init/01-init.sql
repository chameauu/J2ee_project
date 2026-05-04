-- Hospital Management System Database Initialization
-- This script runs automatically when the MySQL container starts for the first time

-- Create database if not exists (already done by docker-compose)
CREATE DATABASE IF NOT EXISTS hospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE hospital_db;

-- Grant privileges to hospital_user
GRANT ALL PRIVILEGES ON hospital_db.* TO 'hospital_user'@'%';
FLUSH PRIVILEGES;

-- The tables will be created automatically by Hibernate (spring.jpa.hibernate.ddl-auto=update)
-- This script is just for initial setup and can be extended with seed data if needed

SELECT 'Database initialized successfully!' AS message;
