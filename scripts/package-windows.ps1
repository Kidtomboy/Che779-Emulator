param(
    [string]$EmulatorJar = "apps/emulator/source/Che779_Emulator.jar",
    [string]$RemoterExe = "apps/remoter/source/bin/Release/net48/Che779_Remoter.exe",
    [string]$OutputDirectory = "artifacts"
)
$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
Push-Location $repo
try {
    $jarPath = (Resolve-Path $EmulatorJar).Path
    $exePath = (Resolve-Path $RemoterExe).Path
    $stage = Join-Path $repo "artifacts/Che779-Project1-Windows"
    $outDir = Join-Path $repo $OutputDirectory
    if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
    if (!(Test-Path $outDir)) { New-Item -ItemType Directory -Path $outDir | Out-Null }
    New-Item -ItemType Directory -Path $stage -Force | Out-Null
    Copy-Item $jarPath (Join-Path $stage "Che779_Emulator.jar")
    Copy-Item $exePath (Join-Path $stage "Che779_Remoter.exe")
    @"
Che779 Project 1 - Windows package
Contains Che779 Remoter and Che779 Emulator V1.
Keep both files in the same directory. Configure Java and game JAR in Remoter.
Application baseline: 0.0.0.2 custom
This package is an official build only when downloaded from the official Che779 repository release.
"@ | Set-Content -Encoding UTF8 (Join-Path $stage "README.txt")
    $hashes = Get-ChildItem $stage -File | Get-FileHash -Algorithm SHA256
    $hashes | ForEach-Object { "{0}  {1}" -f $_.Hash, (Split-Path $_.Path -Leaf) } |
        Set-Content -Encoding ASCII (Join-Path $stage "SHA256SUMS.txt")
    $zipPath = Join-Path $outDir "Che779-Project1-Windows.zip"
    if (Test-Path $zipPath) { Remove-Item $zipPath -Force }
    Compress-Archive -Path (Join-Path $stage "*") -DestinationPath $zipPath -CompressionLevel Optimal
    Write-Host "Package created: $zipPath"
}
finally { Pop-Location }
