@echo off
REM StegoCam - Build Windows Executable
REM This script creates a standalone JAR and optionally a Windows installer

echo ========================================
echo    StegoCam Windows Build Script
echo ========================================
echo.

REM Step 1: Clean and compile
echo [1/3] Cleaning and compiling project...
call mvn clean compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)

REM Step 2: Run tests
echo.
echo [2/3] Running tests...
call mvn test
if %ERRORLEVEL% NEQ 0 (
    echo WARNING: Some tests failed, but continuing...
)

REM Step 3: Package as JAR
echo.
echo [3/3] Creating standalone JAR...
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Packaging failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo    Build Successful!
echo ========================================
echo.
echo Standalone JAR created at:
echo   target\StegoCam-standalone.jar
echo.
echo To run the JAR:
echo   java -jar target\StegoCam-standalone.jar
echo.
echo ========================================
echo.

pause
