param(
    [Parameter(Mandatory = $true)][string]$ModuleFolder,
    [string]$Repository = (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent)
)
$ErrorActionPreference = 'Stop'
$taskRepo = (Resolve-Path -LiteralPath $Repository).Path
$taskModule = (Resolve-Path -LiteralPath $ModuleFolder).Path
$taskManifestPath = Join-Path $taskModule 'MODULE_MANIFEST.json'
$taskManifest = Get-Content -LiteralPath $taskManifestPath -Raw | ConvertFrom-Json
$taskModulePaths = @{'module 1'='frontend/module 1'; 'module 2'='frontend/module 2'; 'module 3'='backend/module 3'; 'module 4'='backend/module 4'}
if (!$taskModulePaths.ContainsKey([string]$taskManifest.moduleId) -or $taskManifest.repositoryModulePath -ne $taskModulePaths[[string]$taskManifest.moduleId]) { throw 'This bundle does not match the three-folder module layout.' }
if ($taskManifest.status -ne 'implemented') { throw 'This bundle does not contain an implemented module.' }
$taskBase = Join-Path $taskRepo 'BASE_CONTRACT.json'
if (!(Test-Path -LiteralPath $taskBase) -or (Get-FileHash -LiteralPath $taskBase -Algorithm SHA256).Hash.ToLowerInvariant() -ne $taskManifest.baseContractHash) {
    throw 'Use the supplied CivicPulse 1.1.0 base repository. This base contract does not match.'
}
foreach ($taskRequired in $taskManifest.requiredModules) {
    $taskStatePath = Join-Path $taskRepo "$($taskModulePaths["module $taskRequired"])/INSTALLED_MODULE.json"
    if (!(Test-Path -LiteralPath $taskStatePath)) { throw "Install module $taskRequired first." }
    $taskState = Get-Content -LiteralPath $taskStatePath -Raw | ConvertFrom-Json
    if ($taskState.moduleId -ne "module $taskRequired" -or $taskState.version -ne $taskManifest.version) { throw 'A prerequisite module version does not match.' }
}
foreach ($taskRecord in $taskManifest.baseFiles) {
    $taskBaseFile = Join-Path $taskRepo $taskRecord.path
    if (!(Test-Path -LiteralPath $taskBaseFile) -or (Get-FileHash -LiteralPath $taskBaseFile -Algorithm SHA256).Hash.ToLowerInvariant() -ne $taskRecord.sha256) {
        throw "Foundation file changed: $($taskRecord.path). Review compatibility before importing."
    }
}
$taskPayload = (Resolve-Path -LiteralPath (Join-Path $taskModule 'payload')).Path
$taskCopies = @()
foreach ($taskRecord in $taskManifest.payloadFiles) {
    $taskRelative = [string]$taskRecord.path
    if ([IO.Path]::IsPathRooted($taskRelative) -or $taskRelative -match '(^|[\\/])\.\.([\\/]|$)' -or $taskRelative -match '(^|[\\/])\.git([\\/]|$)') { throw 'Unsafe module path.' }
    if (!$taskRelative.StartsWith($taskManifest.repositoryModulePath + '/', [StringComparison]::Ordinal)) { throw 'Payload file is outside this module ownership path.' }
    $taskSource = [IO.Path]::GetFullPath((Join-Path $taskPayload $taskRelative))
    $taskTarget = [IO.Path]::GetFullPath((Join-Path $taskRepo $taskRelative))
    if (!$taskSource.StartsWith($taskPayload + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase) -or !$taskTarget.StartsWith($taskRepo + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) { throw 'Module path is outside its allowed directory.' }
    if (!(Test-Path -LiteralPath $taskSource)) { throw "Missing module file: $taskRelative" }
    if ((Get-Item -LiteralPath $taskSource).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Symbolic links are not permitted in module payloads.' }
    $taskHash = (Get-FileHash -LiteralPath $taskSource -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($taskHash -ne $taskRecord.sha256) { throw "Module checksum mismatch: $taskRelative" }
    if (Test-Path -LiteralPath $taskTarget) {
        if ((Get-Item -LiteralPath $taskTarget).Attributes -band [IO.FileAttributes]::ReparsePoint) { throw 'Existing symbolic link conflicts with a module file.' }
        if ((Get-FileHash -LiteralPath $taskTarget -Algorithm SHA256).Hash.ToLowerInvariant() -ne $taskHash) { throw "Existing file differs: $taskRelative. Review and merge your changes before importing." }
    }
    $taskCopies += [PSCustomObject]@{Source=$taskSource; Target=$taskTarget}
}
$taskMarker = Join-Path $taskRepo "$($taskManifest.repositoryModulePath)/INSTALLED_MODULE.json"
if ((Test-Path -LiteralPath $taskMarker) -and (Get-FileHash -LiteralPath $taskMarker -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $taskManifestPath -Algorithm SHA256).Hash) { throw 'A different release of this module is already installed.' }
# Every path/hash/conflict is checked before the first file is copied.
foreach ($taskCopy in $taskCopies) {
    New-Item -ItemType Directory -Path (Split-Path $taskCopy.Target -Parent) -Force | Out-Null
    Copy-Item -LiteralPath $taskCopy.Source -Destination $taskCopy.Target
}
Copy-Item -LiteralPath $taskManifestPath -Destination $taskMarker
Write-Host "$($taskManifest.moduleId) imported: $($taskCopies.Count) verified files."
Write-Host 'Run .\mvnw.cmd verify, review the Git diff, then follow this module''s commit plan.'
