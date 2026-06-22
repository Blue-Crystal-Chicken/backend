@echo off
REM Avvia la cucina (doppio click). Lancia start.ps1 bypassando la policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1"
pause
