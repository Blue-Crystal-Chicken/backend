<#
  start-backend.ps1
  Build + avvio dello stack backend unificato (be_unification): Postgres + RabbitMQ + backend.

  Uso:
    .\start-backend.ps1
#>

$ErrorActionPreference = "Stop"
Set-Location -LiteralPath $PSScriptRoot

Write-Host "1/2 - Build delle immagini Docker..." -ForegroundColor Cyan
docker compose build

Write-Host "2/2 - Avvio dei container..." -ForegroundColor Cyan
docker compose up -d

Write-Host ""
docker compose ps
Write-Host ""
Write-Host "Backend  -> http://localhost:8080" -ForegroundColor Green
Write-Host "RabbitMQ -> http://localhost:15672  (guest/guest)" -ForegroundColor Green
Write-Host "Log dal vivo:  docker compose logs -f backend" -ForegroundColor DarkGray
