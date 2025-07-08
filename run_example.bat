@echo off
echo ========================================
echo Schema Validation Example Runner
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

echo Checking Maven installation...
REM Try to use Maven directly from the known path
if exist "C:\devtool\maven\bin\mvn.cmd" (
    echo Found Maven at C:\devtool\maven\bin\mvn.cmd
    set "MAVEN_HOME=C:\devtool\maven"
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
    echo Maven is available.
) else (
    echo ERROR: Could not find Maven at C:\devtool\maven\bin\mvn.cmd
    echo Please check your Maven installation
    pause
    exit /b 1
)

echo Building and packaging the project...
mvn clean package
if errorlevel 1 (
    echo ERROR: Build failed
    pause
    exit /b 1
)

echo.
echo Checking if jar was created...
if not exist "target\schema-1.0-SNAPSHOT.jar" (
    echo ERROR: Jar file was not created
    echo Checking target directory contents:
    dir target
    pause
    exit /b 1
)

echo.
echo Running SchemaValidationExample...
echo ========================================
echo.

REM Run the example using the jar file
java -jar target/schema-1.0-SNAPSHOT.jar
if errorlevel 1 (
    echo ERROR: Failed to run the application
    pause
    exit /b 1
)

echo.
echo ========================================
echo Example completed
echo ========================================
pause 