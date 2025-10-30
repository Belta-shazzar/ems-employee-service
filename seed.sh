#!/bin/bash

echo "=========================================="
echo "Employee Management System - Database Seeder"
echo "=========================================="
echo ""

# Check if Maven wrapper exists
if [ ! -f "./mvnw" ]; then
    echo "Error: Maven wrapper not found. Please run this script from the employee-service directory."
    exit 1
fi

# Run the application with seed profile
echo "Starting database seeding..."
echo ""

./mvnw spring-boot:run -Dspring-boot.run.profiles=seed -Dspring-boot.run.arguments="--server.port=8088"

echo ""
echo "=========================================="
echo "Seeding process completed!"
echo "=========================================="
echo ""
