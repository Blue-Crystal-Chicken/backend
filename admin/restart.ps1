<#
    Blue Crystal Chicken - Riavvio
    Ferma tutto (stop.ps1) e poi riavvia tutto (start.ps1) in un solo comando.
    Da usare quando l'ambiente è già acceso e vuoi rilanciarlo senza dare 2 comandi.

    Uso:   .\restart.ps1
           senza RabbitMQ:        .\restart.ps1 -NoRabbit
           mantieni RabbitMQ su:  .\restart.ps1 -KeepRabbit
#>

param(
    [switch]$NoRabbit,   # non riavviare Docker/RabbitMQ (passato a start.ps1)
    [switch]$KeepRabbit  # non fermare Docker/RabbitMQ (passato a stop.ps1)
)

$Root  = $PSScriptRoot
$Stop  = Join-Path $Root 'stop.ps1'
$Start = Join-Path $Root 'start.ps1'

if (-not (Test-Path $Stop))  { Write-Host "stop.ps1 non trovato: $Stop"  -ForegroundColor Red; exit 1 }
if (-not (Test-Path $Start)) { Write-Host "start.ps1 non trovato: $Start" -ForegroundColor Red; exit 1 }

Write-Host '=== Blue Crystal Chicken - RIAVVIO ===' -ForegroundColor Cyan

# ── 1. Stop ─────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '>>> STOP' -ForegroundColor Magenta
& $Stop -KeepRabbit:$KeepRabbit

# Lascia liberare le porte prima di rilanciare
Start-Sleep -Seconds 2

# ── 2. Start ────────────────────────────────────────────────────────────────
Write-Host ''
Write-Host '>>> START' -ForegroundColor Magenta
& $Start -NoRabbit:$NoRabbit

Write-Host ''
Write-Host '=== Riavvio completato ===' -ForegroundColor Green
