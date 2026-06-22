@echo off
REM Avvia il tabellone (doppio click). Lancia start.ps1 bypassando la policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1"
pause
