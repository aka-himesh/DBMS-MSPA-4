@echo off
REM ============================================================================
REM PawCare Management System - Compilation Script
REM ============================================================================
echo [PawCare] Compiling Java source files...

if not exist bin mkdir bin

javac -cp ".;lib\mysql-connector-j-9.2.0.jar" -d bin src\main\java\com\pawcare\model\*.java src\main\java\com\pawcare\util\*.java src\main\java\com\pawcare\dao\*.java src\main\java\com\pawcare\service\*.java src\main\java\com\pawcare\*.java

if %ERRORLEVEL% equ 0 (
    echo [PawCare] Compilation SUCCESSFUL! Compiled classes are in 'bin\'.
    echo [PawCare] You can now run the application using 'run.bat'.
) else (
    echo [PawCare] Compilation FAILED. Please check compiler errors above.
)
pause
