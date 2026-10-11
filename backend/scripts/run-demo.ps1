# Builds the WAR and starts CivicPulse with the in-memory H2 demo profile.
# Demo records are recreated on every start. Do not use this profile in production.
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..\..")
.\mvnw.cmd -q -DskipTests package
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -jar backend\target\civicpulse.war --spring.profiles.active=demo @args
