# ============================================================================
#  Manager (web) — START
#  Avvia il dev server di Vite. Installa le dipendenze se mancano e salva il
#  PID in .manager.pid cosi' stop.ps1 puo' fermarlo.
# ============================================================================

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

# --- Porta del dev server: legge VITE_PORT da .env, altrimenti 5185 ----------
$port = 5185
if (Test-Path ".env") {
    $line = Select-String -Path ".env" -Pattern '^\s*VITE_PORT\s*=\s*(\d+)' | Select-Object -First 1
    if ($line) { $port = [int]$line.Matches[0].Groups[1].Value }
}

# --- Gia' in esecuzione? ------------------------------------------------------
$existing = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
if ($existing) {
    Write-Host "Il manager risulta gia' attivo sulla porta $port." -ForegroundColor Yellow
    Write-Host "  -> http://localhost:$port"
    exit 0
}

# --- Dipendenze ---------------------------------------------------------------
if (-not (Test-Path "node_modules")) {
    Write-Host "Installo le dipendenze (npm install)..." -ForegroundColor Cyan
    $env:NODE_OPTIONS = "--use-system-ca"   # workaround proxy/SSL aziendale
    npm install
    if ($LASTEXITCODE -ne 0) { Write-Host "npm install fallito." -ForegroundColor Red; exit 1 }
}

# --- Avvio in background ------------------------------------------------------
Write-Host "Avvio del manager sulla porta $port..." -ForegroundColor Cyan
$proc = Start-Process -FilePath "cmd.exe" `
    -ArgumentList "/c", "npm run dev" `
    -WorkingDirectory $root `
    -WindowStyle Minimized `
    -PassThru

$proc.Id | Out-File -FilePath ".manager.pid" -Encoding ascii

Start-Sleep -Seconds 2
Write-Host ""
Write-Host "Manager avviato (PID $($proc.Id))." -ForegroundColor Green
Write-Host "  -> http://localhost:$port"
Write-Host "Login manager di test: manager@bluecrystal.it / manager123"
Write-Host "Per fermarlo: .\stop.ps1  (oppure doppio click su stop.bat)"
