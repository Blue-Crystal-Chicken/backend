<#
  list-stations.ps1
  Elenca le SEDI con il loro TOKEN-STAZIONE, così sai quale mettere in frontends.env
  per configurare Cucina/Tabellone sulla sede giusta.

  Uso:
    .\list-stations.ps1 -Local                 # backend su localhost
    .\list-stations.ps1 -Ipf 192.168.1.50      # backend sul PC di Giuseppe
    .\list-stations.ps1 -Email a@b.it -Password xxx   # credenziali admin diverse
#>
param(
    [string]$Ipf,
    [switch]$Local,
    [string]$Email = "admin@bluecrystal.it",
    [string]$Password = "admin123"
)

$ErrorActionPreference = "Stop"
if ($Local) { $Ipf = "localhost" }
if (-not $Ipf) {
    $envFile = Join-Path $PSScriptRoot "frontends.env"
    if (Test-Path $envFile) {
        foreach ($line in Get-Content $envFile) { if ($line -match "^\s*IPF\s*=\s*(.*)$") { $Ipf = $Matches[1].Trim() } }
    }
}
if (-not $Ipf -or $Ipf -eq "") { $Ipf = "localhost" }
$B = "http://{0}:8080" -f $Ipf

Write-Host "Backend: $B" -ForegroundColor Cyan

# Login admin (curl.exe --noproxy per evitare il proxy aziendale su localhost)
$body = "{`"email`":`"$Email`",`"password`":`"$Password`"}"
$loginRaw = & curl.exe -s --noproxy '*' -X POST "$B/api/auth/login" -H "Content-Type: application/json" -d $body
$token = ($loginRaw | ConvertFrom-Json).token
if (-not $token) { Write-Error "Login fallito (credenziali o backend non raggiungibile)."; return }

$locsRaw = & curl.exe -s --noproxy '*' "$B/api/locations"
$locs = $locsRaw | ConvertFrom-Json

Write-Host ""
Write-Host ("{0,-3} {1,-24} {2,-12} {3}" -f "ID", "SEDE", "CITTA", "STATION_TOKEN") -ForegroundColor Yellow
Write-Host ("-" * 90)
foreach ($l in $locs) {
    $tokRaw = & curl.exe -s --noproxy '*' -H "Authorization: Bearer $token" "$B/api/locations/$($l.id)/station-token"
    $tok = ($tokRaw | ConvertFrom-Json).stationToken
    $city = ""
    if ($l.address) { $city = $l.address.city }
    Write-Host ("{0,-3} {1,-24} {2,-12} {3}" -f $l.id, $l.name, $city, $tok)
}
Write-Host ""
Write-Host "Copia il token della sede desiderata in frontends.env (STATION_TOKEN, o CUCINA_TOKEN/TABELLONE_TOKEN)." -ForegroundColor DarkGray
