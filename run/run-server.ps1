$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
	Write-Error "Java runtime not found in PATH. Install JDK/JRE or open this script from a Developer Command Prompt."
	exit 1
}

$config = Join-Path $PSScriptRoot "release\config.properties"
if (-not (Test-Path $config)) {
	Write-Error "Missing config.properties at $config"
	exit 1
}

Write-Host "Starting ChatServer..." -ForegroundColor Cyan
Write-Host "Using config: $config" -ForegroundColor DarkGray
Write-Host "Press Ctrl+C to stop the server." -ForegroundColor Yellow

java -Dfile.encoding=UTF-8 -jar ".\server.jar"
