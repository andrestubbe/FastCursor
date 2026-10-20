@echo off
setlocal
cd /d "%~dp0"
echo ====================================================
echo Starting FastCursor Interactive Demo
echo ====================================================

java --enable-preview --enable-native-access=ALL-UNNAMED -cp "target\FastCursor-0.1.0.jar;examples\Demo\src\main\java" fastcursor.Demo
pause
