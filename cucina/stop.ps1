# ============================================================================
#  Cucina (KDS) — STOP
#  Ferma il dev server di Vite: prima il processo salvato in .cucina.pid,
#  poi (per sicurezza) qualunque processo in ascolto sulla porta.
# ============================================================================

$ErrorActionPreference = "SilentlyContinue"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

# --- Porta del dev server: legge VITE_PORT da .env, altrimenti 5183 ----------
$port = 5183
if (Test-Path ".env") {
    $line = Select-String -Path ".env" -Pattern '^\s*VITE_PORT\s*=\s*(\d+)' | Select-Object -First 1
    if ($line) { $port = [int]$line.Matches[0].Groups[1].Value }
}

$killed = $false

# --- 1. Albero del processo salvato (cmd -> npm -> node) ----------------------
if (Test-Path ".cucina.pid") {
    $pidValue = (Get-Content ".cucina.pid" | Select-Object -First 1).Trim()
    if ($pidValue) {
        taskkill /PID $pidValue /T /F | Out-Null
        $killed = $true
    }
    Remove-Item ".cucina.pid" -Force
}

# --- 2. Sicurezza: chiudi chi resta in ascolto sulla porta -------------------
$conns = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
foreach ($c in $conns) {
    Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
    $killed = $true
}

if ($killed) {
    Write-Host "Cucina fermata (porta $port liberata)." -ForegroundColor Green
} else {
    Write-Host "Nessuna cucina in esecuzione sulla porta $port." -ForegroundColor Yellow
}
