# Đóng gói bản Windows (Emulator + Remoter) thành file zip trên máy của bạn.
# Chạy sau khi đã build: build_emulator.ps1 và build_remoter.ps1 (hoặc dotnet build).
param(
    [string]$EmulatorJar = "apps/emulator/source/Che779_Emulator.jar",
    [string]$RemoterExe  = "apps/remoter-windows/source/Che779_Remoter.exe",
    [string]$OutputDirectory = "artifacts"
)
$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
Push-Location $repo
try {
    $ver = (Get-Content VERSION -Raw).Trim()
    $name = "Che779-Emulator-$ver-Windows"
    $stage = Join-Path $repo "$OutputDirectory/$name"
    if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
    New-Item -ItemType Directory -Path $stage -Force | Out-Null
    Copy-Item (Resolve-Path $EmulatorJar).Path (Join-Path $stage "Che779_Emulator.jar")
    Copy-Item (Resolve-Path $RemoterExe).Path (Join-Path $stage "Che779_Remoter.exe")
    @"
Che779 Emulator + Remoter $ver cho Windows

1. Cần Java 8 trở lên (https://adoptium.net).
2. Giữ Che779_Emulator.jar và Che779_Remoter.exe CÙNG MỘT THƯ MỤC.
3. Bấm đúp Che779_Remoter.exe, chọn file game .jar rồi bấm Bắt đầu.
"@ | Set-Content -Encoding UTF8 (Join-Path $stage "DOC-TOI-DAY.txt")
    $zip = Join-Path $repo "$OutputDirectory/$name.zip"
    if (Test-Path $zip) { Remove-Item $zip -Force }
    Compress-Archive -Path $stage -DestinationPath $zip -CompressionLevel Optimal
    # Mã SHA-256 tính sau cùng, trên đúng file zip vừa tạo.
    $h = (Get-FileHash $zip -Algorithm SHA256).Hash.ToLower()
    "$h  $name.zip" | Set-Content -Encoding ASCII (Join-Path $repo "$OutputDirectory/SHA256SUMS.txt")
    Write-Host "Đã tạo: $zip"
}
finally { Pop-Location }
