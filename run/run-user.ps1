$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

$config = Join-Path $PSScriptRoot "release\config.properties"
if (-not (Test-Path $config)) {
    Write-Error "Missing config.properties at $config"
    exit 1
}

Write-Host "Starting User client..." -ForegroundColor Cyan
java -Dfile.encoding=UTF-8 -jar ".\user.jar"
