@echo off
REM ============================================================================
REM PawCare Management System - Run Script
REM ============================================================================
if not exist "bin\com\pawcare\Main.class" (
    echo [PawCare] Compiled classes not found. Running compilation first...
    call compile.bat
)

echo [PawCare] Starting PawCare Management System...
java -cp "bin;lib\mysql-connector-j-9.2.0.jar;." com.pawcare.Main
pause
