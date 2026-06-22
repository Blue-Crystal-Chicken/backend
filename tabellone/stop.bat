@echo off
REM Ferma il tabellone (doppio click). Lancia stop.ps1 bypassando la policy.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop.ps1"
pause
