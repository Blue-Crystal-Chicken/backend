@echo off
REM Avvia il manager (doppio click). Lancia start.ps1 bypassando la policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1"
pause
