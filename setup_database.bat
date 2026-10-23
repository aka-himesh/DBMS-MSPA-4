@echo off
REM ============================================================================
REM PawCare Database Setup Script (MySQL 8.0+)
REM Initializes schema and populates sample data into pawcare_db
REM ============================================================================
setlocal enabledelayedexpansion

echo ============================================================================
echo   PawCare Management System - MySQL Database Initialization
echo ============================================================================

set "MYSQL_PATH=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist "%MYSQL_PATH%" (
    where mysql >nul 2>nul
    if %ERRORLEVEL% equ 0 (
        set "MYSQL_PATH=mysql"
    ) else (
        echo [ERROR] mysql.exe was not found in default MySQL 8.0 location or system PATH.
        echo Please ensure MySQL is installed and running.
        pause
        exit /b 1
    )
)

echo Found MySQL executable: "%MYSQL_PATH%"
set /p MYSQL_USER=Enter MySQL username [default: root]: 
if "%MYSQL_USER%"=="" set MYSQL_USER=root

echo.
echo Please enter your MySQL password for user '%MYSQL_USER%':
set /p MYSQL_PWD=Password: 

echo.
echo [1/2] Creating database schema 'pawcare_db'...
"%MYSQL_PATH%" -u %MYSQL_USER% -p%MYSQL_PWD% < "database\schema.sql"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to execute database\schema.sql. Check password and try again.
    pause
    exit /b 1
)

echo [2/2] Loading sample data records...
"%MYSQL_PATH%" -u %MYSQL_USER% -p%MYSQL_PWD% < "database\sample_data.sql"
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Failed to execute database\sample_data.sql.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Database 'pawcare_db' created and populated with sample data!
echo Updating db.properties with provided user credentials...
(
    echo # PawCare Database Configuration
    echo db.url=jdbc:mysql://localhost:3306/pawcare_db?useSSL=false^&allowPublicKeyRetrieval=true^&serverTimezone=UTC
    echo db.user=%MYSQL_USER%
    echo db.password=%MYSQL_PWD%
) > db.properties

echo [SUCCESS] db.properties updated successfully!
echo You can now run 'run.bat' to launch the system.
echo.
pause
