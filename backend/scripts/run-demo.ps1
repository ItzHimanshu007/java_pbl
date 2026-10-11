param(
    [ValidateRange(1024, 65535)][int]$Port = 8080,
    [string]$SocketDirectory = (Join-Path ([Environment]::GetFolderPath('LocalApplicationData')) 'CivicPulse\sockets')
)
$ErrorActionPreference = 'Stop'
$taskProjectRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$taskWar = Join-Path $taskProjectRoot 'backend\target\civicpulse.war'
if (!(Test-Path -LiteralPath $taskWar)) {
    throw 'Build the project first: run .\mvnw.cmd verify from the repository root.'
}
if ($env:JAVA_HOME) {
    $taskJava = Join-Path $env:JAVA_HOME 'bin\java.exe'
} else {
    $taskJava = (Get-Command java.exe -ErrorAction Stop).Source
}
New-Item -ItemType Directory -Path $SocketDirectory -Force | Out-Null
Write-Host "CivicPulse local demonstration: http://localhost:$Port"
& $taskJava "-Djdk.net.unixdomain.tmpdir=$SocketDirectory" '-jar' $taskWar '--spring.profiles.active=demo' '--server.address=127.0.0.1' "--server.port=$Port"
if ($LASTEXITCODE -ne 0) { throw 'CivicPulse stopped with an error. Read the startup message above.' }
