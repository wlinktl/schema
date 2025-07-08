@echo off
echo ========================================
echo Schema Validation Example Runner (Direct)
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

echo.
echo Running SchemaValidationExample directly...
echo ========================================
echo.

REM Run the example directly using Java classpath
java -cp "src/main/java;src/main/resources;lib/*" com.demo.schema.SchemaValidationExample

echo.
echo ========================================
echo Example completed
echo ========================================
pause 