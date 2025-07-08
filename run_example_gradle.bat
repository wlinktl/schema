@echo off
echo ========================================
echo Schema Validation Example Runner (Gradle)
echo ========================================
echo.

echo Checking Java installation...
java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java and try again
    pause
    exit /b 1
)
echo Java is available.

echo Checking Gradle installation...
gradle -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Gradle is not installed or not in PATH
    echo Please install Gradle and try again
    pause
    exit /b 1
)
echo Gradle is available.

echo Building the project...
gradle build -q
if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo Running SchemaValidationExample...
echo ========================================
echo.

REM Run the example using Gradle
gradle run -q

echo.
echo ========================================
echo Example completed
echo ========================================
pause 