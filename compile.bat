@echo off
setlocal
cd /d "%~dp0"

echo ====================================================
echo  Compiling FastCursor (Java 21+ FFM)
echo ====================================================

call mvn clean package
if %errorlevel% neq 0 (
    echo [ERROR] Maven build failed!
    pause
    exit /b %errorlevel%
)

echo [SUCCESS] Build completed: target\FastCursor-0.1.0.jar
