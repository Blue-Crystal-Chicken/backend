<#
  start-frontends.ps1
  Avvia INSIEME Admin, Manager, Cucina e Tabellone, puntandoli al backend su un IP.

  Uso (un comando solo):
    .\start-frontends.ps1 -Ipf 192.168.1.50    # backend sul PC di Giuseppe (IP)
    .\start-frontends.ps1 -Local               # backend sul TUO localhost (rapido)
    .\start-frontends.ps1 -Ipf 192.168.1.50 -StationToken <token-sede>
    .\start-frontends.ps1                       # legge IPF/STATION_TOKEN da frontends.env

  Cosa fa: per ogni frontend scrive il suo file .env con l'IP indicato e lancia
  `npm run dev` in una finestra separata (così partono tutti in parallelo).
#>
param(
    [string]$Ipf,
    [string]$StationToken,    # token usato da cucina+tabellone se non specificati i singoli
    [string]$CucinaToken,     # token-stazione per la Cucina (legacy/opzionale)
    [string]$TabelloneToken,  # token-stazione per il Tabellone (legacy/opzionale)
    [string]$LocationId,      # id sede comune per Cucina+Tabellone (modo semplice)
    [string]$CucinaId,        # id sede della Cucina (override)
    [string]$TabelloneId,     # id sede del Tabellone (override)
    [switch]$Local            # forza il backend su localhost (ignora IP/frontends.env)
)

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

function Get-EnvValue([string]$file, [string]$key) {
    if (Test-Path $file) {
        foreach ($line in Get-Content $file) {
            if ($line -match "^\s*$key\s*=\s*(.*)$") { return $Matches[1].Trim() }
        }
    }
    return $null
}

# IP: -Local forza localhost; altrimenti -Ipf; altrimenti da frontends.env; altrimenti localhost
$envFile = Join-Path $root "frontends.env"
if ($Local) { $Ipf = "localhost" }
if (-not $Ipf) { $Ipf = Get-EnvValue $envFile "IPF" }
if (-not $Ipf -or $Ipf -eq "") { $Ipf = "localhost" }
if (-not $StationToken) { $StationToken = Get-EnvValue $envFile "STATION_TOKEN" }
if (-not $StationToken) { $StationToken = "" }
# Token per-stazione: override singolo, altrimenti da frontends.env, altrimenti lo STATION_TOKEN comune
if (-not $CucinaToken)    { $CucinaToken    = Get-EnvValue $envFile "CUCINA_TOKEN" }
if (-not $CucinaToken)    { $CucinaToken    = $StationToken }
if (-not $TabelloneToken) { $TabelloneToken = Get-EnvValue $envFile "TABELLONE_TOKEN" }
if (-not $TabelloneToken) { $TabelloneToken = $StationToken }
# Id sede (modo semplice, senza token): override singolo, poi env, poi comune
if (-not $LocationId)    { $LocationId    = Get-EnvValue $envFile "LOCATION_ID" }
if (-not $LocationId)    { $LocationId    = "" }
if (-not $CucinaId)      { $CucinaId      = Get-EnvValue $envFile "CUCINA_LOCATION_ID" }
if (-not $CucinaId)      { $CucinaId      = $LocationId }
if (-not $TabelloneId)   { $TabelloneId   = Get-EnvValue $envFile "TABELLONE_LOCATION_ID" }
if (-not $TabelloneId)   { $TabelloneId   = $LocationId }

$apiUrl   = "http://{0}:8080" -f $Ipf
$notifUrl = "http://{0}:8085" -f $Ipf
Write-Host "Backend -> $apiUrl   (notifiche $notifUrl)" -ForegroundColor Cyan

# Definizione dei frontend: nome, sottocartella, contenuto .env
$frontends = @(
    @{ name = "admin";     dir = "admin\blue-crystal-chicken-auth\blue-crystal-chicken"; cmd = "npm run dev";
       env = @("VITE_API_URL=$apiUrl", "VITE_NOTIFICATION_API_URL=$notifUrl") },
    @{ name = "manager";   dir = "manager"; cmd = "npm run dev";
       env = @("VITE_API_URL=$apiUrl", "VITE_NOTIFICATION_API_URL=$notifUrl") },
    @{ name = "cucina";    dir = "cucina"; cmd = "npm run dev";
       env = @("VITE_API_HOST=$Ipf", "VITE_API_PORT=8080", "VITE_LOCATION_ID=$CucinaId", "VITE_STATION_TOKEN=$CucinaToken") },
    @{ name = "tabellone"; dir = "tabellone"; cmd = "npm run dev";
       env = @("VITE_API_HOST=$Ipf", "VITE_API_PORT=8080", "VITE_LOCATION_ID=$TabelloneId", "VITE_STATION_TOKEN=$TabelloneToken") },
    @{ name = "cassa";     dir = "counter-main\counter-main"; cmd = "pnpm run dev";
       env = @("VITE_API_BASE_URL=$apiUrl") }
)

foreach ($fe in $frontends) {
    $path = Join-Path $root $fe.dir
    if (-not (Test-Path $path)) { Write-Warning "Salto $($fe.name): cartella non trovata ($path)"; continue }
    # Scrive il .env del frontend (ascii = niente BOM, che romperebbe il parsing di Vite)
    $fe.env | Set-Content -Path (Join-Path $path ".env") -Encoding ascii
    # Installer giusto in base al comando (pnpm per la cassa, npm per gli altri)
    if ($fe.cmd -like "pnpm*") { $installer = "pnpm install" } else { $installer = "npm install" }
    # Se mancano le dipendenze le installa (con --use-system-ca per il proxy aziendale),
    # poi avvia il dev server (nuova finestra)
    $run = "Set-Location -LiteralPath '$path'; " +
           "if (-not (Test-Path 'node_modules')) { Write-Host 'Installo dipendenze ($($fe.name))...' -ForegroundColor Yellow; `$env:NODE_OPTIONS='--use-system-ca'; $installer }; " +
           "$($fe.cmd)"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $run
    Write-Host "Avviato $($fe.name)  ($path)" -ForegroundColor Green
}

Write-Host ""
Write-Host "Tutti i frontend avviati e puntati al backend su $Ipf." -ForegroundColor Cyan
Write-Host "Admin :5173  Manager :5185  Cucina :5183  Tabellone :5180  Cassa :8081" -ForegroundColor DarkGray
