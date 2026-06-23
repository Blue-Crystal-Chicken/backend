<#
  stop-backend.ps1
  Ferma lo stack backend unificato (be_unification) e chiude le finestre PowerShell.

  Uso:
    .\stop-backend.ps1
#>

$ErrorActionPreference = "SilentlyContinue"
Set-Location -LiteralPath $PSScriptRoot

Write-Host "Fermo i container (docker compose down)..." -ForegroundColor Cyan
docker compose down

Write-Host "Chiudo le altre finestre PowerShell (tranne questa)..." -ForegroundColor Cyan
Get-Process powershell, pwsh -ErrorAction SilentlyContinue |
    Where-Object { $_.Id -ne $PID } |
    ForEach-Object { try { Stop-Process -Id $_.Id -Force } catch {} }

Write-Host ""
Write-Host "Stack backend fermato e finestre PowerShell chiuse." -ForegroundColor Green
