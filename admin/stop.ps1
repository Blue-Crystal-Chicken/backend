<#
    Blue Crystal Chicken - Stop automatizzato
    Ferma Backend (porta 8080), Frontend (Vite, porta 5173) e RabbitMQ (Docker).

    Uso:   .\stop.ps1
           mantieni RabbitMQ acceso:   .\stop.ps1 -KeepRabbit
           porte diverse:              .\stop.ps1 -BackendPort 8080 -FrontendPort 5173
#>

param(
    [int]$BackendPort  = 8080,
    [int]$FrontendPort = 5173,
    [switch]$KeepRabbit  # non fermare Docker/RabbitMQ
)

$Root = $PSScriptRoot
Write-Host '=== Blue Crystal Chicken - stop ===' -ForegroundColor Cyan

# ── Helper: termina chi ascolta su una porta ────────────────────────────────
function Stop-Port {
    param([int]$Port, [string]$Label)

    $conns = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if (-not $conns) {
        Write-Host "  $Label (porta $Port): nessun processo in ascolto." -ForegroundColor DarkGray
        return
    }

    $procIds = $conns | Select-Object -ExpandProperty OwningProcess -Unique
    foreach ($procId in $procIds) {
        if ($procId -eq 0) { continue }
        try {
            $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
            $name = if ($proc) { $proc.ProcessName } else { 'sconosciuto' }
            Stop-Process -Id $procId -Force -ErrorAction Stop
            Write-Host "  $Label (porta $Port): fermato PID $procId ($name)." -ForegroundColor Green
        } catch {
            Write-Host "  $Label (porta $Port): impossibile fermare PID $procId - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

# ── Helper: chiude le finestre PowerShell aperte da start.ps1 ────────────────
function Close-StartedWindows {
    $closed = $false

    # 1) Per PID salvati da start.ps1
    $pidFile = Join-Path $Root '.bcc-windows.pids'
    if (Test-Path $pidFile) {
        foreach ($line in (Get-Content $pidFile)) {
            $procId = 0
            if ([int]::TryParse($line.Trim(), [ref]$procId) -and $procId -gt 0) {
                try {
                    Stop-Process -Id $procId -Force -ErrorAction Stop
                    Write-Host "  Finestra PID $procId chiusa." -ForegroundColor Green
                    $closed = $true
                } catch {
                    # finestra gia chiusa o PID non piu valido: ignora
                }
            }
        }
        Remove-Item $pidFile -ErrorAction SilentlyContinue
    }

    # 2) Fallback: per titolo finestra (se il file PID manca o e incompleto)
    $byTitle = Get-Process powershell, pwsh -ErrorAction SilentlyContinue |
        Where-Object { $_.MainWindowTitle -eq 'BCC Backend' -or $_.MainWindowTitle -eq 'BCC Frontend' }
    foreach ($p in $byTitle) {
        try {
            Stop-Process -Id $p.Id -Force -ErrorAction Stop
            Write-Host "  Finestra '$($p.MainWindowTitle)' (PID $($p.Id)) chiusa." -ForegroundColor Green
            $closed = $true
        } catch { }
    }

    if (-not $closed) {
        Write-Host '  Nessuna finestra BCC da chiudere.' -ForegroundColor DarkGray
    }
}

# ── 1. Backend ──────────────────────────────────────────────────────────────
Write-Host '[1/4] Stop Backend...' -ForegroundColor Yellow
Stop-Port -Port $BackendPort -Label 'Backend'

# ── 2. Frontend ─────────────────────────────────────────────────────────────
Write-Host '[2/4] Stop Frontend...' -ForegroundColor Yellow
Stop-Port -Port $FrontendPort -Label 'Frontend'

# ── 3. Chiusura finestre PowerShell aperte da start.ps1 ─────────────────────
Write-Host '[3/4] Chiusura finestre PowerShell...' -ForegroundColor Yellow
Close-StartedWindows

# ── 4. RabbitMQ (Docker) ────────────────────────────────────────────────────
if (-not $KeepRabbit) {
    Write-Host '[4/4] Stop RabbitMQ (docker compose down)...' -ForegroundColor Yellow
    try {
        Push-Location $Root
        docker compose down
        if ($LASTEXITCODE -eq 0) {
            Write-Host '  RabbitMQ fermato.' -ForegroundColor Green
        } else {
            Write-Host '  Docker ha risposto con errore (forse gia spento).' -ForegroundColor DarkYellow
        }
        Pop-Location
    } catch {
        Write-Host '  Docker non disponibile: niente da fermare.' -ForegroundColor DarkYellow
    }
} else {
    Write-Host '[4/4] RabbitMQ lasciato acceso (-KeepRabbit).' -ForegroundColor DarkGray
}

Write-Host ''
Write-Host '=== Stop completato ===' -ForegroundColor Green
