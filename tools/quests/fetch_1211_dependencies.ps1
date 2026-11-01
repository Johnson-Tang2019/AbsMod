# Fetch pinned optional runtime mods for the 1.21.1 integration fixtures.
$ErrorActionPreference = 'Stop'
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$module = Join-Path $root '1.21.1NeoForge'
$destination = Join-Path $module 'run/mods'
New-Item -ItemType Directory -Force -Path $destination | Out-Null
$manifest = Get-Content -LiteralPath (Join-Path $module 'runtime-mods.json') -Raw | ConvertFrom-Json
foreach ($entry in $manifest) {
    $target = Join-Path $destination $entry.file
    if (!(Test-Path -LiteralPath $target) -or (Get-FileHash -LiteralPath $target).Hash -ne $entry.sha256) {
        Invoke-WebRequest -Uri $entry.url -OutFile $target
    }
    if ((Get-FileHash -LiteralPath $target).Hash -ne $entry.sha256) { throw "Dependency checksum mismatch: $($entry.file)" }
}
