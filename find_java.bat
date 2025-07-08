@echo off
echo ========================================
echo Finding Java Installations
echo ========================================
echo.

echo Current Java version:
java -version
echo.

echo Checking common Java installation paths:
echo.

if exist "C:\devtool\jdk21\bin\java.exe" (
    echo Found Java at: C:\devtool\jdk21\bin\java.exe
    C:\devtool\jdk21\bin\java.exe -version
    echo.
)

if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    echo Found Java at: C:\Program Files\Java\jdk-21\bin\java.exe
    "C:\Program Files\Java\jdk-21\bin\java.exe" -version
    echo.
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-21\bin\java.exe" (
    echo Found Java at: C:\Program Files\Eclipse Adoptium\jdk-21\bin\java.exe
    "C:\Program Files\Eclipse Adoptium\jdk-21\bin\java.exe" -version
    echo.
)

if exist "C:\Program Files\OpenJDK\jdk-21\bin\java.exe" (
    echo Found Java at: C:\Program Files\OpenJDK\jdk-21\bin\java.exe
    "C:\Program Files\OpenJDK\jdk-21\bin\java.exe" -version
    echo.
)

echo ========================================
pause 