@echo off
setlocal
cd /d "%~dp0"
echo ====================================================
echo Starting FastCursor Cycle Demo (Every 3s)
echo ====================================================

java --enable-preview -cp "target\classes;examples\src\main\java" demo.CursorCycleDemo
pause
