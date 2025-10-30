# Database Seeder Guide

## Overview

The database seeder automatically creates initial data for the Employee Management System, including:
- An admin user
- A manager user  
- An employee user
- Sample departments

## Running the Seeder

### Prerequisites
- PostgreSQL database must be running
- Database `ems` must be created
- Employee Service must be able to connect to the database

### Commands

**On Linux/Mac:**
```bash
./seed.sh
```

**On Windows:**
```bash
seed.bat
```

**Or manually:**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=seed
```

## What Gets Created

### Users

| Role | Email | First Name | Last Name | Department | Status |
|------|-------|------------|-----------|------------|--------|
| ADMIN | admin@company.com | System | Administrator | Administration | ACTIVE |
| MANAGER | manager@company.com | Jane | Manager | Engineering | ACTIVE |
| EMPLOYEE | employee@company.com | John | Employee | Engineering | ACTIVE |

### Departments

1. **Administration** - For admin users
2. **Engineering** - For technical staff
3. **Human Resources** - For HR staff
4. **Sales** - For sales staff

## After Seeding

### Important Notes

1. **User IDs**: The seeder will output the UUID for each created user. **Copy these IDs** - you'll need them to set passwords!

2. **No Passwords Set**: The seeder only creates employee records. You must set passwords using the Auth Service.

3. **No Kafka Events**: The seeder bypasses the normal employee creation flow, so no email notifications are sent.

### Setting Passwords

For each user, you need to set a password using the Auth Service:

```bash
# Set password for admin
curl -X POST http://localhost:8082/api/auth/set-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "ADMIN_USER_ID",
    "password": "YourSecurePassword123"
  }'

# Set password for manager
curl -X POST http://localhost:8082/api/auth/set-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "MANAGER_USER_ID",
    "password": "YourSecurePassword123"
  }'

# Set password for employee
curl -X POST http://localhost:8082/api/auth/set-password \
  -H "Content-Type: application/json" \
  -d '{
    "token": "EMPLOYEE_USER_ID",
    "password": "YourSecurePassword123"
  }'
```

### Logging In

After setting passwords, you can login:

```bash
# Login as admin
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@company.com",
    "password": "YourSecurePassword123"
  }'
```

## Idempotency

The seeder is **idempotent** - it checks if the admin user already exists before creating data. If the admin user exists, the seeder will skip all operations and exit gracefully.

This means you can safely run the seeder multiple times without creating duplicate data.

## Troubleshooting

### "Admin user already exists"

This is normal if you've already run the seeder. The system is working correctly.

### Database Connection Error

Ensure:
1. PostgreSQL is running: `docker ps | grep ems_pg`
2. Database exists: `docker exec -it ems_pg psql -U ems -l`
3. Employee Service can connect (check application.yml)

### "Table doesn't exist"

The Employee Service should automatically run Flyway migrations on startup. If tables don't exist:
1. Check Flyway is enabled in application.yml
2. Check migration files exist in `src/main/resources/db/migration`
3. Restart the Employee Service

## Profile Configuration

The seeder uses the Spring profile `seed`. This profile:
- Runs only when explicitly activated
- Executes on application startup via `CommandLineRunner`
- Exits after seeding is complete

## Customization

To customize the seeded data, edit:
```
src/main/java/com/ems/employeeservice/seeder/DatabaseSeeder.java
```

You can:
- Change user details (names, emails)
- Add more users
- Add more departments
- Modify user roles or statuses

## Security Note

In production:
- Use secure, randomly generated passwords
- Store initial credentials securely
- Force password change on first login
- Consider using environment variables for sensitive data
