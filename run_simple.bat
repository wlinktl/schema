@echo off
echo ========================================
echo Simple Schema Validation Runner
echo ========================================
echo.

REM Set Maven path
set "MAVEN_HOME=C:\devtool\maven"
set "PATH=%MAVEN_HOME%\bin;%PATH%"

echo Checking Java version...
java -version

echo Building project...
call mvn clean package

echo.
echo Running application...
echo ========================================
java -jar target/schema-1.0-SNAPSHOT.jar

echo.
echo ========================================
echo Done!
pause 