@echo off
setlocal
cd /d "%~dp0"

REM Launch the PowerShell runner and keep the window open
powershell -ExecutionPolicy Bypass -NoExit -File "%~dp0run-server.ps1"
