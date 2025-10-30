@echo off
echo ==========================================
echo Employee Management System - Database Seeder
echo ==========================================
echo.

REM Check if Maven wrapper exists
if not exist "mvnw.cmd" (
    echo Error: Maven wrapper not found. Please run this script from the employee-service directory.
    exit /b 1
)

REM Run the application with seed profile
echo Starting database seeding...
echo.

call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=seed

echo.
echo ==========================================
echo Seeding process completed!
echo ==========================================
echo.
echo Next steps:
echo 1. Use the Auth Service to set passwords for the created users
echo 2. Example: POST http://localhost:8082/api/auth/set-password
echo    Body: { "token": "<employee-id>", "password": "YourPassword123" }
echo.
echo 3. Login with: POST http://localhost:8082/api/auth/login
echo    Body: { "email": "admin@company.com", "password": "YourPassword123" }
echo.
pause
