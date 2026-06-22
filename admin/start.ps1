<#
    Blue Crystal Chicken - Avvio automatizzato
    Lancia RabbitMQ (Docker), Backend (Spring Boot) e Frontend (Vite)
    ciascuno nella propria finestra PowerShell.

    Uso:   click destro > "Esegui con PowerShell"
           oppure dal terminale:  .\start.ps1
           salta RabbitMQ:        .\start.ps1 -NoRabbit
#>

param(
    [switch]$NoRabbit  # usa questo flag per non avviare Docker/RabbitMQ
)

# ── Percorsi ────────────────────────────────────────────────────────────────
$Root     = $PSScriptRoot
$Backend  = Join-Path $Root 'backend'
$Frontend = Join-Path $Root 'blue-crystal-chicken-auth\blue-crystal-chicken'
$Mvn      = 'C:\Program Files\Java\apache-maven-3.9.12\bin\mvn.cmd'

Write-Host '=== Blue Crystal Chicken - avvio ===' -ForegroundColor Cyan

# ── Verifiche rapide ────────────────────────────────────────────────────────
if (-not (Test-Path $Mvn))      { Write-Host "Maven non trovato in: $Mvn" -ForegroundColor Red; exit 1 }
if (-not (Test-Path $Backend))  { Write-Host "Cartella backend non trovata: $Backend" -ForegroundColor Red; exit 1 }
if (-not (Test-Path $Frontend)) { Write-Host "Cartella frontend non trovata: $Frontend" -ForegroundColor Red; exit 1 }

# ── 1. RabbitMQ (Docker) ────────────────────────────────────────────────────
if (-not $NoRabbit) {
    Write-Host '[1/3] Avvio RabbitMQ (docker compose up -d)...' -ForegroundColor Yellow
    try {
        Push-Location $Root
        docker compose up -d
        if ($LASTEXITCODE -eq 0) {
            Write-Host '      RabbitMQ OK - UI: http://localhost:15672 (guest/guest)' -ForegroundColor Green
        } else {
            Write-Host '      Docker ha risposto con errore: l app parte comunque senza notifiche.' -ForegroundColor DarkYellow
        }
        Pop-Location
    } catch {
        Write-Host '      Docker non disponibile: salto RabbitMQ (l app parte comunque).' -ForegroundColor DarkYellow
    }
} else {
    Write-Host '[1/3] RabbitMQ saltato (-NoRabbit).' -ForegroundColor DarkGray
}

# ── 2. Backend (Spring Boot) in una nuova finestra ──────────────────────────
Write-Host '[2/3] Avvio Backend (Spring Boot, porta 8080) in nuova finestra...' -ForegroundColor Yellow
$beWindow = Start-Process powershell -PassThru -ArgumentList @(
    '-NoExit', '-Command',
    "`$host.UI.RawUI.WindowTitle='BCC Backend'; Set-Location '$Backend'; Write-Host 'BACKEND - Spring Boot' -ForegroundColor Cyan; & '$Mvn' spring-boot:run"
)

# Lascia al backend qualche secondo per occupare la porta / iniziare il seeding
Start-Sleep -Seconds 5

# ── 3. Frontend (Vite) in una nuova finestra ────────────────────────────────
Write-Host '[3/3] Avvio Frontend (Vite) in nuova finestra...' -ForegroundColor Yellow
$feWindow = Start-Process powershell -PassThru -ArgumentList @(
    '-NoExit', '-Command',
    "`$host.UI.RawUI.WindowTitle='BCC Frontend'; Set-Location '$Frontend'; Write-Host 'FRONTEND - Vite' -ForegroundColor Cyan; npm run dev"
)

# ── Salva i PID delle finestre cosi stop.ps1 puo chiuderle ───────────────────
$pidFile = Join-Path $Root '.bcc-windows.pids'
@($beWindow.Id, $feWindow.Id) | Set-Content -Path $pidFile -Encoding ascii

Write-Host ''
Write-Host '=== Avvio completato ===' -ForegroundColor Green
Write-Host 'Backend:  http://localhost:8080' -ForegroundColor Gray
Write-Host 'Frontend: http://localhost:5173 (vedi la finestra Vite per l URL esatto)' -ForegroundColor Gray
Write-Host 'Login:    gspptesse@gmail.com / 123456' -ForegroundColor Gray
Write-Host ''
Write-Host 'Per fermare tutto e chiudere le finestre:  .\stop.ps1' -ForegroundColor DarkGray
