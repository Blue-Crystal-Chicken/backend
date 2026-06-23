<#
  stop-frontends.ps1
  Ferma TUTTI i frontend avviati con start-frontends.ps1 e chiude le finestre PowerShell.

  Uso:
    .\stop-frontends.ps1

  Cosa fa:
   1) ferma i dev server in ascolto sulle porte dei frontend (vite/node);
   2) termina eventuali processi node legati a vite rimasti;
   3) chiude tutte le altre finestre PowerShell (tranne questa).
#>

$ErrorActionPreference = "SilentlyContinue"

# 1) Ferma i processi in ascolto sulle porte dei frontend
$ports = 5173, 5185, 5183, 5180, 8081
foreach ($p in $ports) {
    Get-NetTCPConnection -LocalPort $p -State Listen | ForEach-Object {
        try {
            Stop-Process -Id $_.OwningProcess -Force
            Write-Host "Fermato dev server sulla porta $p (PID $($_.OwningProcess))" -ForegroundColor Green
        } catch {}
    }
}

# 2) Per sicurezza: termina i processi node/vite eventualmente rimasti
Get-CimInstance Win32_Process -Filter "Name='node.exe'" |
    Where-Object { $_.CommandLine -match "vite" } |
    ForEach-Object { try { Stop-Process -Id $_.ProcessId -Force } catch {} }

# 3) Chiude tutte le altre finestre PowerShell (NON questa, identificata da $PID)
Get-Process powershell, pwsh -ErrorAction SilentlyContinue |
    Where-Object { $_.Id -ne $PID } |
    ForEach-Object { try { Stop-Process -Id $_.Id -Force } catch {} }

Write-Host ""
Write-Host "Tutti i frontend fermati e le finestre PowerShell chiuse." -ForegroundColor Cyan
